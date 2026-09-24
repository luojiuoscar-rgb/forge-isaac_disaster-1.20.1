package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/** Distance-weighted force preserves inertia: outgoing bullets slow down and return. */
public final class MyReflectionBulletTrajectoryModule extends TrajectoryModule {
    @Override
    public int priority() {
        return TrajectoryModulePriority.MY_REFLECTION_BULLET.priority();
    }

    public static final double PULL_ACCELERATION = 0.18;
    public static final double VERTICAL_PULL_RATIO = 0.15;
    public static final double STRONG_PULL_DISTANCE = 12;
    public static final double STRONG_PULL_TRANSITION = 6;
    public static final double NEAR_PULL_RATIO = 0.1;

    @Override
    public void initialize(TrajectoryContext ctx) {
        TrajectoryPathState state = ctx.runtimeState.path();
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
    public TrajectoryMotion apply(TrajectoryContext ctx) {
        initialize(ctx);
        TrajectoryPathState state = ctx.runtimeState.path();
        Vec3 target = ctx.bulletObject.getTrajectoryRuntime().origin();
        if (state.reflectionFollowsOwner()) {
            var owner = ctx.bulletObject.getOwner();
            // Client prediction has the synchronized owner anchor but no entity references.
            target =
                owner != null ? owner.position() : ctx.trajectoryPos.add(0, -state.height() * 0.6, 0);
        }
        double speed = ctx.baseVelocity.length();
        double remaining = Math.max(0, ctx.bulletObject.getRange() - ctx.bulletObject.getTraveled());
        double dt = Math.max(0, ctx.deltaTicks);
        Vec3 velocity = ctx.velocity;
        Vec3 position = ctx.position;
        int count = Math.max(1, (int) Math.ceil(dt / 0.1));
        var path = new ArrayList<Vec3>();
        double distance = 0;
        for (int i = 0; i < count; i++) {
            Vec3 toTarget = target.subtract(position);
            Vec3 pullDirection = new Vec3(toTarget.x, toTarget.y * VERTICAL_PULL_RATIO, toTarget.z);
            // Speed squared keeps the turning distance comparable at different shot speeds.
            Vec3 force =
                pullDirection.normalize().scale(pullAtDistance(toTarget.length()) * speed * speed);
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
        ctx.runtimeState.path().resume();
        ctx.runtimeState.path().advance(distance);
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
}
