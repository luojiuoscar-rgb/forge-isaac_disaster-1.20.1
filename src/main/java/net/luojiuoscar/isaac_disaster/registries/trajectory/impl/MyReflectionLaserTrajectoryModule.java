package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** A C1 left-entry, U-turn and symmetric return in the immutable firing plane. */
public final class MyReflectionLaserTrajectoryModule
        extends TrajectoryModule<MyReflectionLaserTrajectoryModule.State> {
    public static final double RADIUS = 1.5;
    public static final double TRANSITION = 2;
    public static final double STRAIGHT = 3;
    public static final double RECOVERY = 2;
    public static final double PRELUDE = 2 * (TRANSITION + STRAIGHT) + Math.PI * RADIUS;

    public static final class State implements TrajectoryState {
        private double distance;
        private int stage;
        private boolean initialized;
        private boolean suspended;
        private double startDistance;
        private boolean pathStarted;
        private Vec3 launchOrigin = Vec3.ZERO;
        private Vec3 launchAxis = new Vec3(1, 0, 0);
        private Vec3 planeSide = Vec3.ZERO;
        private double recoveryStart = -2;
        private Vec3 recoveryOffset = Vec3.ZERO;
        private Vec3 recoveryTangent = Vec3.ZERO;

        public double distance() {
            return distance;
        }

        public void distance(double value) {
            distance = finite(value) ? Math.max(0, value) : 0;
        }

        public int stage() {
            return stage;
        }

        public void stage(int value) {
            stage = value;
        }

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

        public double startDistance() {
            return startDistance;
        }

        public void startDistance(double value) {
            startDistance = finite(value) ? Math.max(0, value) : 0;
        }

        public boolean pathStarted() {
            return pathStarted;
        }

        public void pathStarted(boolean value) {
            pathStarted = value;
        }

        public Vec3 launchOrigin() {
            return launchOrigin;
        }

        public void launchOrigin(Vec3 value) {
            launchOrigin = value == null ? Vec3.ZERO : value;
        }

        public Vec3 launchAxis() {
            return launchAxis;
        }

        public void launchAxis(Vec3 value) {
            launchAxis =
                    value == null || value.lengthSqr() < 1e-12
                            ? new Vec3(1, 0, 0)
                            : value.normalize();
        }

        public Vec3 planeSide() {
            return planeSide;
        }

        public void planeSide(Vec3 value) {
            planeSide = value == null ? Vec3.ZERO : value;
        }

        public double recoveryStart() {
            return recoveryStart;
        }

        public void recoveryStart(double value) {
            recoveryStart = finite(value) ? value : -2;
        }

        public Vec3 recoveryOffset() {
            return recoveryOffset;
        }

        public void recoveryOffset(Vec3 value) {
            recoveryOffset = value == null ? Vec3.ZERO : value;
        }

        public Vec3 recoveryTangent() {
            return recoveryTangent;
        }

        public void recoveryTangent(Vec3 value) {
            recoveryTangent = value == null ? Vec3.ZERO : value;
        }

        private static boolean finite(double value) {
            return Double.isFinite(value);
        }

        @Override
        public void rebase() {
            initialized = false;
            suspended = true;
            recoveryOffset = Vec3.ZERO;
            recoveryTangent = Vec3.ZERO;
        }

        @Override
        public State copy() {
            State copy = new State();
            copy.distance = distance;
            copy.stage = stage;
            copy.initialized = initialized;
            copy.suspended = suspended;
            copy.startDistance = startDistance;
            copy.pathStarted = pathStarted;
            copy.launchOrigin = launchOrigin;
            copy.launchAxis = launchAxis;
            copy.planeSide = planeSide;
            copy.recoveryStart = recoveryStart;
            copy.recoveryOffset = recoveryOffset;
            copy.recoveryTangent = recoveryTangent;
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
        buffer.writeDouble(state.distance());
        buffer.writeVarInt(state.stage());
        buffer.writeBoolean(state.initialized());
        buffer.writeBoolean(state.suspended());
        buffer.writeDouble(state.startDistance());
        buffer.writeBoolean(state.pathStarted());
        writeVec(buffer, state.launchOrigin());
        writeVec(buffer, state.launchAxis());
        writeVec(buffer, state.planeSide());
        buffer.writeDouble(state.recoveryStart());
        writeVec(buffer, state.recoveryOffset());
        writeVec(buffer, state.recoveryTangent());
    }

    @Override
    public State readState(FriendlyByteBuf buffer) {
        State state = new State();
        state.distance(buffer.readDouble());
        state.stage(buffer.readVarInt());
        state.initialized(buffer.readBoolean());
        if (buffer.readBoolean()) state.suspend();
        else state.resume();
        state.startDistance(buffer.readDouble());
        state.pathStarted(buffer.readBoolean());
        state.launchOrigin(readVec(buffer));
        state.launchAxis(readVec(buffer));
        state.planeSide(readVec(buffer));
        state.recoveryStart(buffer.readDouble());
        state.recoveryOffset(readVec(buffer));
        state.recoveryTangent(readVec(buffer));
        return state;
    }

    @Override
    public int priority() {
        return TrajectoryModulePriority.MY_REFLECTION_LASER.priority();
    }

    @Override
    public double maximumFreeDistance(int amplifier) {
        return PRELUDE;
    }

    @Override
    public double maximumFreeDistance(int amplifier, List<TrajectorySpec> specs) {
        return PRELUDE + planetExitDistance(specs);
    }

    private static double planetExitDistance(List<TrajectorySpec> specs) {
        return specs.stream()
                .filter(s -> s.id().equals(ModTrajectoryModules.TINY_PLANET_LASER.getId()))
                .mapToDouble(
                        s ->
                                TinyPlanetTrajectoryModule.BASE_RADIUS
                                        + Math.max(0, s.amplifier())
                                                * TinyPlanetTrajectoryModule.RADIUS_PER_LEVEL
                                        + TinyPlanetTrajectoryModule.EXIT_DISTANCE)
                .sum();
    }

    @Override
    protected void initializeTyped(TrajectoryContext ctx, State state) {
        if (state.initialized()) {
            if (!state.launchOrigin().equals(ctx.bulletObject.getTrajectoryRuntime().origin())) {
                state.launchOrigin(ctx.bulletObject.getTrajectoryRuntime().origin());
                state.suspend();
            }
            return;
        }
        state.launchOrigin(ctx.bulletObject.getTrajectoryRuntime().origin());
        state.launchAxis(ctx.bulletObject.getTrajectoryRuntime().launchDirection());
        Vec3 left = new Vec3(0, 1, 0).cross(state.launchAxis());
        if (ctx.bulletObject.getShooter() instanceof Entity shooter) {
            double yaw = Math.toRadians(shooter.getYRot());
            Vec3 playerLeft = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
            Vec3 projected =
                    playerLeft.subtract(
                            state.launchAxis().scale(playerLeft.dot(state.launchAxis())));
            if (projected.lengthSqr() > 1e-10) left = projected;
        }
        if (left.lengthSqr() < 1e-10) left = new Vec3(1, 0, 0);
        state.planeSide(left.normalize());
        state.initialized(true);
    }

    @Override
    protected TrajectoryMotion applyTyped(TrajectoryContext ctx, State state) {
        initialize(ctx);
        if (!state.pathStarted()) {
            state.startDistance(planetExitDistance(ctx.bulletObject.getTrajectorySpecs()));
            state.pathStarted(true);
            state.suspend();
        }
        double before = state.distance();
        double freeEnd = PRELUDE + state.startDistance();
        double remaining =
                Math.max(0, ctx.bulletObject.getRange() - ctx.bulletObject.getTraveled());
        double increment = Math.min(stepDistance(ctx), remaining + Math.max(0, freeEnd - before));
        if (state.suspended()) {
            state.recoveryStart(before);
            state.recoveryOffset(ctx.input.position().subtract(point(state, before)));
            Vec3 heading =
                    ctx.input.velocity().lengthSqr() > 1e-12
                            ? ctx.input.velocity().normalize()
                            : state.launchAxis();
            state.recoveryTangent(heading.subtract(tangent(state, before)));
            state.resume();
        }
        double after = before + increment;
        Vec3 next = point(state, after).add(recovery(state, after));
        Vec3 velocity =
                tangent(state, after)
                        .add(recoveryDerivative(state, after))
                        .scale(ctx.input.baseVelocity().length());
        var path = new ArrayList<Vec3>();
        var pathCosts = new ArrayList<Double>();
        int count = Math.max(1, (int) Math.ceil(increment / 0.1));
        for (int i = 1; i <= count; i++) {
            double d = before + increment * i / count;
            path.add(point(state, d).add(recovery(state, d)));
            double freeBefore = Math.max(0, freeEnd - (before + increment * (i - 1) / count));
            pathCosts.add(Math.max(0, increment / count - freeBefore));
        }
        state.distance(after);
        state.stage(after < freeEnd ? 0 : 1);
        double charge = Math.min(remaining, Math.max(0, increment - Math.max(0, freeEnd - before)));
        return new TrajectoryMotion(
                next,
                velocity,
                Vec3.ZERO,
                increment / Math.max(1e-12, ctx.input.deltaTicks()),
                TrajectoryMotion.CompositionMode.PRIMARY,
                charge,
                path,
                pathCosts);
    }

    @Override
    public double progressDistance(TrajectoryState state) {
        return castState(state).distance();
    }

    private static Vec3 point(State p, double distance) {
        double x, y;
        double a = p.startDistance();
        double d = Math.max(0, distance);
        if (d < TRANSITION) {
            x = a + d;
            y = RADIUS * smooth(d / TRANSITION);
        } else if ((d -= TRANSITION) < STRAIGHT) {
            x = a + TRANSITION + d;
            y = RADIUS;
        } else if ((d -= STRAIGHT) < Math.PI * RADIUS) {
            double angle = d / RADIUS;
            x = a + TRANSITION + STRAIGHT + RADIUS * Math.sin(angle);
            y = RADIUS * Math.cos(angle);
        } else if ((d -= Math.PI * RADIUS) < STRAIGHT + a) {
            x = a + TRANSITION + STRAIGHT - d;
            y = -RADIUS;
        } else if ((d -= STRAIGHT + a) < TRANSITION) {
            x = TRANSITION - d;
            y = -RADIUS * smooth(x / TRANSITION);
        } else {
            x = -(d - TRANSITION);
            y = 0;
        }
        return p.launchOrigin().add(p.launchAxis().scale(x)).add(p.planeSide().scale(y));
    }

    private static Vec3 tangent(State p, double d) {
        if (d < TRANSITION)
            return p.launchAxis()
                    .add(
                            p.planeSide()
                                    .scale(RADIUS / TRANSITION * smoothDerivative(d / TRANSITION)));
        d -= TRANSITION;
        if (d < STRAIGHT) return p.launchAxis();
        d -= STRAIGHT;
        if (d < Math.PI * RADIUS)
            return p.launchAxis()
                    .scale(Math.cos(d / RADIUS))
                    .subtract(p.planeSide().scale(Math.sin(d / RADIUS)));
        d -= Math.PI * RADIUS;
        if (d < STRAIGHT + p.startDistance()) return p.launchAxis().scale(-1);
        d -= STRAIGHT + p.startDistance();
        if (d < TRANSITION)
            return p.launchAxis()
                    .scale(-1)
                    .add(
                            p.planeSide()
                                    .scale(
                                            RADIUS
                                                    / TRANSITION
                                                    * smoothDerivative(1 - d / TRANSITION)));
        return p.launchAxis().scale(-1);
    }

    private static double smooth(double t) {
        return t * t * t * (10 + t * (-15 + 6 * t));
    }

    private static double smoothDerivative(double t) {
        return 30 * t * t * (1 - t) * (1 - t);
    }

    private static Vec3 recovery(State p, double distance) {
        double t = TrajectoryFrame.clamp((distance - p.recoveryStart()) / RECOVERY, 0, 1);
        return p.recoveryOffset()
                .scale(2 * t * t * t - 3 * t * t + 1)
                .add(p.recoveryTangent().scale(RECOVERY * (t * t * t - 2 * t * t + t)));
    }

    private static Vec3 recoveryDerivative(State p, double distance) {
        double t = TrajectoryFrame.clamp((distance - p.recoveryStart()) / RECOVERY, 0, 1);
        return p.recoveryOffset()
                .scale((6 * t * t - 6 * t) / RECOVERY)
                .add(p.recoveryTangent().scale(3 * t * t - 4 * t + 1));
    }

    private static Vec3 readVec(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    private static void writeVec(FriendlyByteBuf buffer, Vec3 value) {
        buffer.writeDouble(value.x);
        buffer.writeDouble(value.y);
        buffer.writeDouble(value.z);
    }
}
