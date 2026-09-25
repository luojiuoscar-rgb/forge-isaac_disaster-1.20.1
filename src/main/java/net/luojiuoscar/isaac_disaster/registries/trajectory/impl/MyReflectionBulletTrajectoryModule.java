package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryFrame;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModulePriority;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/** Distance-weighted force preserves inertia: outgoing bullets slow down and return. */
public final class MyReflectionBulletTrajectoryModule
        extends TrajectoryModule<MyReflectionBulletTrajectoryModule.State> {
    public static final double PULL_ACCELERATION = 0.18;
    public static final double VERTICAL_PULL_RATIO = 0.15;
    public static final double STRONG_PULL_DISTANCE = 12;
    public static final double STRONG_PULL_TRANSITION = 6;
    public static final double NEAR_PULL_RATIO = 0.1;

    public static final class State implements TrajectoryState {
        private boolean initialized;
        private boolean suspended;
        private boolean reflectionFollowsOwner;
        private double height = 1.8;
        private double distance;

        public boolean initialized() {
            return initialized;
        }

        public void initialized(boolean value) {
            initialized = value;
        }

        public boolean suspended() {
            return suspended;
        }

        @Override
        public void suspend() {
            suspended = true;
        }

        @Override
        public void resume() {
            suspended = false;
        }

        public boolean reflectionFollowsOwner() {
            return reflectionFollowsOwner;
        }

        public void reflectionFollowsOwner(boolean value) {
            reflectionFollowsOwner = value;
        }

        public double height() {
            return height;
        }

        public void height(double value) {
            height = Double.isFinite(value) ? value : 1.8;
        }

        public double distance() {
            return distance;
        }

        public void advance(double value) {
            if (Double.isFinite(value) && value > 0) distance += value;
        }

        @Override
        public State copy() {
            State copy = new State();
            copy.initialized = initialized;
            copy.suspended = suspended;
            copy.reflectionFollowsOwner = reflectionFollowsOwner;
            copy.height = height;
            copy.distance = distance;
            return copy;
        }
    }

    @Override
    public State createState() {
        return new State();
    }

    @Override
    protected Class<State> stateClass() {
        return State.class;
    }

    @Override
    public void writeState(FriendlyByteBuf buffer, State state) {
        buffer.writeBoolean(state.initialized());
        buffer.writeBoolean(state.suspended());
        buffer.writeBoolean(state.reflectionFollowsOwner());
        buffer.writeDouble(state.height());
        buffer.writeDouble(state.distance());
    }

    @Override
    public State readState(FriendlyByteBuf buffer) {
        State state = new State();
        state.initialized(buffer.readBoolean());
        if (buffer.readBoolean()) state.suspend();
        else state.resume();
        state.reflectionFollowsOwner(buffer.readBoolean());
        state.height(buffer.readDouble());
        state.distance = Math.max(0, buffer.readDouble());
        return state;
    }

    @Override
    public int priority() {
        return TrajectoryModulePriority.MY_REFLECTION_BULLET.priority();
    }

    @Override
    protected void initializeTyped(TrajectoryContext ctx, State state) {
        if (state.initialized()) return;
        var owner = ctx.bulletObject.getOwner();
        state.reflectionFollowsOwner(owner != null && ctx.bulletObject.getShooter() == owner);
        if (owner != null) state.height(owner.getBbHeight());
        state.initialized(true);
    }

    public static double pullAtDistance(double distance) {
        double near = smooth(distance / STRONG_PULL_DISTANCE);
        double far = smooth((distance - STRONG_PULL_DISTANCE) / STRONG_PULL_TRANSITION);
        return PULL_ACCELERATION * (NEAR_PULL_RATIO * near + (1 - NEAR_PULL_RATIO) * far);
    }

    private static double smooth(double value) {
        double t = TrajectoryFrame.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    @Override
    protected TrajectoryMotion applyTyped(TrajectoryContext ctx, State state) {
        initialize(ctx);
        Vec3 target = ctx.bulletObject.getTrajectoryRuntime().origin();
        if (state.reflectionFollowsOwner()) {
            var owner = ctx.bulletObject.getOwner();
            target =
                    owner != null
                            ? owner.position()
                            : ctx.trajectoryPos.add(0, -state.height() * 0.6, 0);
        }
        double speed = ctx.baseVelocity.length();
        double remaining =
                Math.max(0, ctx.bulletObject.getRange() - ctx.bulletObject.getTraveled());
        double dt = Math.max(0, ctx.deltaTicks);
        Vec3 velocity = ctx.velocity;
        Vec3 position = ctx.position;
        int count = Math.max(1, (int) Math.ceil(dt / 0.1));
        var path = new ArrayList<Vec3>();
        double distance = 0;
        for (int i = 0; i < count; i++) {
            Vec3 toTarget = target.subtract(position);
            Vec3 pullDirection = new Vec3(toTarget.x, toTarget.y * VERTICAL_PULL_RATIO, toTarget.z);
            Vec3 force =
                    pullDirection.lengthSqr() > 1e-12
                            ? pullDirection
                                    .normalize()
                                    .scale(pullAtDistance(toTarget.length()) * speed * speed)
                            : Vec3.ZERO;
            velocity = velocity.add(force.scale(dt / count));
            if (velocity.length() > speed) velocity = velocity.normalize().scale(speed);
            Vec3 step = velocity.scale(dt / count);
            double length = step.length();
            if (length > remaining - distance) {
                step = step.scale(Math.max(0, remaining - distance) / length);
                length = step.length();
            }
            position = position.add(step);
            distance += length;
            path.add(position);
            if (distance >= remaining) break;
        }
        state.resume();
        state.advance(distance);
        return new TrajectoryMotion(
                position,
                velocity,
                Vec3.ZERO,
                velocity,
                dt > 0 ? distance / dt : 0,
                TrajectoryMotion.ControlMode.INTENT,
                TrajectoryMotion.CompositionMode.RELATIVE,
                distance,
                ctx.composed ? java.util.List.of() : path);
    }

    @Override
    public double progressDistance(TrajectoryState state) {
        return castState(state).distance();
    }
}
