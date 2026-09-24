package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Horizontal player-centered orbit parameterized by travel, with C1 entry and exit. */
public abstract class TinyPlanetTrajectoryModule extends TrajectoryModule {
    public static final double BASE_RADIUS = 3;
    public static final double RADIUS_PER_LEVEL = 1;
    public static final double LAUNCH_DISTANCE = 1;
    public static final double ENTRY_DISTANCE = 2;
    public static final double EXIT_DISTANCE = 2;
    public static final double RECOVERY_DISTANCE = 2;
    public static final double HEIGHT_PITCH_LIMIT_DEGREES = 60;

    public static double heightRatio(Vec3 launchAxis) {
        double pitch = Math.toDegrees(Math.asin(TrajectoryFrame.clamp(launchAxis.y, -1, 1)));
        return TrajectoryFrame.clamp(0.5 + 0.4 * pitch / HEIGHT_PITCH_LIMIT_DEGREES, 0.1, 0.9);
    }

    @Override
    public double maximumFreeDistance(int amplifier) {
        return LAUNCH_DISTANCE
            + ENTRY_DISTANCE
            + 2 * Math.PI * (BASE_RADIUS + Math.max(0, amplifier) * RADIUS_PER_LEVEL)
            + EXIT_DISTANCE;
    }

    @Override
    public void initialize(TrajectoryContext ctx) {
        TrajectoryPathState state = ctx.runtimeState.path();
        Vec3 anchor = ctx.trajectoryPos;
        if (!state.initialized()) {
            state.launchAxis(ctx.bulletObject.getTrajectoryRuntime().launchDirection());
            state.launchOffset(ctx.position.subtract(anchor));
            state.launchOrigin(ctx.position);
            if (ctx.bulletObject.getShooter() instanceof Entity shooter)
                state.height(shooter.getBbHeight());
            state.heightRatio(heightRatio(state.launchAxis()));
            if (state.rotationSign() == 0) {
                state.rotationSign(
                    ctx.bulletObject
                                instanceof net.luojiuoscar.isaac_disaster.bullet.core.BulletState bullet
                            && bullet.slot() >= 0
                            && (bullet.slot() & 1) != 0
                        ? -1
                        : 1);
            }
            state.initialized(true);
        }
    }

    @Override
    public TrajectoryMotion apply(TrajectoryContext ctx) {
        initialize(ctx);
        TrajectoryPathState state = ctx.runtimeState.path();
        Vec3 anchor = ctx.trajectoryPos;
        boolean laser = usesLaserPath();
        double radius = BASE_RADIUS + Math.max(0, ctx.amplifier) * RADIUS_PER_LEVEL;
        double before = state.distance();
        double end = maximumFreeDistance(ctx.amplifier);
        double remaining = Math.max(0, ctx.bulletObject.getRange() - ctx.bulletObject.getTraveled());
        double increment =
            Math.min(stepDistance(ctx), remaining + (laser ? Math.max(0, end - before) : 0));
        double after = before + increment;
        if (!laser) return ordinaryMotion(ctx, state, anchor, radius, increment);
        Vec3 p0 = point(state, anchor, radius, laser, before);
        if (state.suspended()) {
            state.recoveryStart(before);
            state.recoveryOffset(ctx.position.subtract(p0));
            double speed = ctx.baseVelocity.length();
            state.recoveryTangent(
                (speed > 1e-9 ? ctx.velocity.scale(1 / speed) : state.launchAxis())
                    .subtract(tangent(state, anchor, radius, laser, before)));
            state.resume();
        }
        Vec3 next = point(state, anchor, radius, laser, after).add(recovery(state, after));
        Vec3 velocity =
            tangent(state, anchor, radius, laser, after)
                .add(
                    recovery(state, after + 1e-4)
                        .subtract(recovery(state, Math.max(before, after - 1e-4)))
                        .scale(1 / (after + 1e-4 - Math.max(before, after - 1e-4))))
                .scale(ctx.baseVelocity.length());
        state.distance(after);
        ctx.runtimeState.phase(Math.max(0, after - LAUNCH_DISTANCE - ENTRY_DISTANCE) / radius);
        ctx.runtimeState.stage(
            after < LAUNCH_DISTANCE + ENTRY_DISTANCE
                ? 0
                : after < end - EXIT_DISTANCE ? 1 : after < end ? 2 : 3);
        // Do not subtract two large path distances: their rounding can erase the last range residue.
        double charge = Math.min(remaining, Math.max(0, increment - Math.max(0, end - before)));
        java.util.List<Vec3> path = new java.util.ArrayList<>();
        java.util.List<Double> pathCosts = new java.util.ArrayList<>();
        int segments = Math.max(1, (int) Math.ceil(increment / 0.1));
        for (int i = 1; i <= segments; i++) {
            double d = before + increment * i / segments;
            path.add(point(state, anchor, radius, laser, d).add(recovery(state, d)));
            double freeBefore = Math.max(0, end - (before + increment * (i - 1) / segments));
            pathCosts.add(Math.max(0, increment / segments - freeBefore));
        }
        return new TrajectoryMotion(
            next,
            velocity,
            Vec3.ZERO,
            velocity,
            ctx.baseVelocity.length(),
            TrajectoryMotion.ControlMode.INTENT,
            TrajectoryMotion.CompositionMode.PRIMARY,
            charge,
            path,
            pathCosts);
    }

    protected boolean usesLaserPath() {
        return false;
    }

    private TrajectoryMotion ordinaryMotion(
        TrajectoryContext ctx,
        TrajectoryPathState state,
        Vec3 anchor,
        double radius,
        double increment) {
        double before = state.distance();
        if (state.suspended()) {
            state.recoveryStart(before);
            state.recoveryTangent(
                ctx.velocity.lengthSqr() > 1e-12
                    ? ctx.velocity.scale(1 / ctx.velocity.length())
                    : state.launchAxis());
            state.resume();
        }
        Vec3 position = ctx.position;
        Vec3 lastMovement = Vec3.ZERO;
        java.util.List<Vec3> path = new java.util.ArrayList<>();
        int segments = Math.max(1, (int) Math.ceil(increment / 0.05));
        double step = increment / segments;
        Vec3 center =
            new Vec3(anchor.x, anchor.y + (state.heightRatio() - 0.6) * state.height(), anchor.z);
        for (int i = 1; i <= segments; i++) {
            double distance = before + step * i;
            Vec3 target =
                distance <= LAUNCH_DISTANCE + ENTRY_DISTANCE
                    ? point(state, anchor, radius, false, distance)
                    : orbitStep(position, center, radius, step, state.launchAxis(), state.rotationSign());
            Vec3 movement = limited(target.subtract(position), step);
            if (distance < state.recoveryStart() + RECOVERY_DISTANCE) {
                double blend =
                    TrajectoryFrame.smootherstep((distance - state.recoveryStart()) / RECOVERY_DISTANCE);
                movement = limited(state.recoveryTangent().scale(step).lerp(movement, blend), step);
            }
            position = position.add(movement);
            lastMovement = movement;
            path.add(position);
        }
        state.advance(increment);
        ctx.runtimeState.stage(state.distance() < LAUNCH_DISTANCE + ENTRY_DISTANCE ? 0 : 1);
        if (increment > 0)
            ctx.runtimeState.phase(Math.atan2(position.z - center.z, position.x - center.x));
        double dt = Math.max(0, ctx.deltaTicks);
        Vec3 velocity = dt > 0 ? lastMovement.scale(segments / dt) : Vec3.ZERO;
        return new TrajectoryMotion(
            position,
            velocity,
            Vec3.ZERO,
            velocity,
            ctx.baseVelocity.length(),
            TrajectoryMotion.ControlMode.INTENT,
            TrajectoryMotion.CompositionMode.PRIMARY,
            increment,
            path);
    }

    /** Spend a bounded step on height/radius correction, then use its remainder for orbiting. */
    private static Vec3 orbitStep(
        Vec3 position, Vec3 center, double radius, double step, Vec3 launchAxis, int rotationSign) {
        Vec3 radial = new Vec3(position.x - center.x, 0, position.z - center.z);
        double currentRadius = radial.length();
        Vec3 direction =
            currentRadius > 1e-9
                ? radial.scale(1 / currentRadius)
                : new Vec3(launchAxis.x, 0, launchAxis.z);
        if (direction.lengthSqr() < 1e-10) direction = new Vec3(1, 0, 0);
        else direction = direction.scale(1 / direction.length());
        double dy = TrajectoryFrame.clamp(center.y - position.y, -step, step);
        double horizontalBudget = Math.sqrt(Math.max(0, step * step - dy * dy));
        double dr = TrajectoryFrame.clamp(radius - currentRadius, -horizontalBudget, horizontalBudget);
        double nextRadius = Math.max(0, currentRadius + dr);
        double tangentialSquared = Math.max(0, horizontalBudget * horizontalBudget - dr * dr);
        double angle =
            currentRadius * nextRadius > 1e-12
                ? 2
                    * Math.asin(
                        Math.min(1, Math.sqrt(tangentialSquared / (4 * currentRadius * nextRadius))))
                : 0;
        Vec3 side = new Vec3(-direction.z, 0, direction.x).scale(rotationSign < 0 ? -1 : 1);
        Vec3 next =
            center
                .add(direction.scale(nextRadius * Math.cos(angle)))
                .add(side.scale(nextRadius * Math.sin(angle)));
        return new Vec3(next.x, position.y + dy, next.z);
    }

    private static Vec3 limited(Vec3 vector, double length) {
        double magnitude = vector.length();
        return magnitude > length && magnitude > 0 ? vector.scale(length / magnitude) : vector;
    }

    private static Vec3 recovery(TrajectoryPathState state, double distance) {
        double t = TrajectoryFrame.clamp((distance - state.recoveryStart()) / RECOVERY_DISTANCE, 0, 1);
        return hermite(
            state.recoveryOffset(),
            state.recoveryTangent().scale(RECOVERY_DISTANCE),
            Vec3.ZERO,
            Vec3.ZERO,
            t);
    }

    private static Vec3 tangent(
        TrajectoryPathState state, Vec3 anchor, double radius, boolean laser, double distance) {
        double lo = Math.max(0, distance - 1e-4);
        return point(state, anchor, radius, laser, distance + 1e-4)
            .subtract(point(state, anchor, radius, laser, lo))
            .scale(1 / (distance + 1e-4 - lo));
    }

    private static Vec3 point(
        TrajectoryPathState state, Vec3 anchor, double radius, boolean laser, double distance) {
        Vec3 axis = state.launchAxis();
        Vec3 horizontal;
        if (laser) {
            Vec3 vertical = new Vec3(0, 1, 0);
            Vec3 normal = vertical.subtract(axis.scale(vertical.dot(axis)));
            if (normal.lengthSqr() < 1.0E-10) normal = new Vec3(1, 0, 0);
            normal = normal.normalize();
            horizontal = axis;
        } else {
            horizontal = new Vec3(axis.x, 0, axis.z);
            if (horizontal.lengthSqr() < 1e-10) horizontal = new Vec3(1, 0, 0);
            else horizontal = horizontal.normalize();
        }
        Vec3 right =
            laser
                ? axis.cross(new Vec3(0, 1, 0))
                : new Vec3(-horizontal.z, 0, horizontal.x).scale(state.rotationSign());
        if (right.lengthSqr() < 1.0E-10D) right = new Vec3(1, 0, 0);
        else right = right.normalize();
        Vec3 origin = laser ? anchor.add(state.launchOffset()) : state.launchOrigin();
        double y = anchor.y + (state.heightRatio() - 0.6) * state.height();
        Vec3 center = new Vec3(anchor.x, y, anchor.z);
        Vec3 entry = center.add(horizontal.scale(radius));
        if (distance <= LAUNCH_DISTANCE) return origin.add(axis.scale(distance));
        double orbitStart = LAUNCH_DISTANCE + ENTRY_DISTANCE;
        if (distance < orbitStart) {
            return hermite(
                origin.add(axis.scale(LAUNCH_DISTANCE)),
                axis.scale(ENTRY_DISTANCE),
                entry,
                right.scale(ENTRY_DISTANCE),
                (distance - LAUNCH_DISTANCE) / ENTRY_DISTANCE);
        }
        double orbitLength = 2 * Math.PI * radius;
        double orbitEnd = orbitStart + orbitLength;
        if (!laser || distance <= orbitEnd) {
            double angle = (distance - orbitStart) / radius;
            return center
                .add(horizontal.scale(radius * Math.cos(angle)))
                .add(right.scale(radius * Math.sin(angle)));
        }
        Vec3 exit = origin.add(axis.scale(radius + EXIT_DISTANCE));
        if (distance < orbitEnd + EXIT_DISTANCE) {
            return hermite(
                entry,
                right.scale(EXIT_DISTANCE),
                exit,
                axis.scale(EXIT_DISTANCE),
                (distance - orbitEnd) / EXIT_DISTANCE);
        }
        return exit.add(axis.scale(distance - orbitEnd - EXIT_DISTANCE));
    }

    private static Vec3 hermite(Vec3 p0, Vec3 m0, Vec3 p1, Vec3 m1, double t) {
        double t2 = t * t, t3 = t2 * t;
        return p0.scale(2 * t3 - 3 * t2 + 1)
            .add(m0.scale(t3 - 2 * t2 + t))
            .add(p1.scale(-2 * t3 + 3 * t2))
            .add(m1.scale(t3 - t2));
    }
}
