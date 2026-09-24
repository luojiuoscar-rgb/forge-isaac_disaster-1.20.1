package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** A C1 left-entry, U-turn and symmetric return in the immutable firing plane. */
public final class MyReflectionLaserTrajectoryModule extends TrajectoryModule {
    @Override
    public int priority() {
        return TrajectoryModulePriority.MY_REFLECTION_LASER.priority();
    }

    public static final double RADIUS = 1.5;
    public static final double TRANSITION = 2;
    public static final double STRAIGHT = 3;
    public static final double RECOVERY = 2;
    public static final double PRELUDE = 2 * (TRANSITION + STRAIGHT) + Math.PI * RADIUS;

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
                        + Math.max(0, s.amplifier()) * TinyPlanetTrajectoryModule.RADIUS_PER_LEVEL
                        + TinyPlanetTrajectoryModule.EXIT_DISTANCE)
            .sum();
    }

    @Override
    public void initialize(TrajectoryContext ctx) {
        TrajectoryPathState p = ctx.runtimeState.path();
        if (p.initialized()) {
            if (!p.launchOrigin().equals(ctx.bulletObject.getTrajectoryRuntime().origin())) {
                p.launchOrigin(ctx.bulletObject.getTrajectoryRuntime().origin());
                p.suspend();
            }
            return;
        }
        p.launchOrigin(ctx.bulletObject.getTrajectoryRuntime().origin());
        p.launchAxis(ctx.bulletObject.getTrajectoryRuntime().launchDirection());
        Vec3 left = new Vec3(0, 1, 0).cross(p.launchAxis());
        if (ctx.bulletObject.getShooter() instanceof Entity shooter) {
            double yaw = Math.toRadians(shooter.getYRot());
            Vec3 playerLeft = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
            Vec3 projected = playerLeft.subtract(p.launchAxis().scale(playerLeft.dot(p.launchAxis())));
            if (projected.lengthSqr() > 1e-10) left = projected;
        }
        if (left.lengthSqr() < 1e-10) {
            left = new Vec3(1, 0, 0);
        }
        p.planeSide(left.normalize());
        p.initialized(true);
    }

    @Override
    public TrajectoryMotion apply(TrajectoryContext ctx) {
        initialize(ctx);
        TrajectoryPathState p = ctx.runtimeState.path();
        if (!p.pathStarted()) {
            // Planet exits ahead of the origin. Extend the return leg back to the real firing point.
            p.startDistance(planetExitDistance(ctx.bulletObject.getTrajectorySpecs()));
            p.pathStarted(true);
            p.suspend();
        }
        double before = p.distance();
        double freeEnd = PRELUDE + p.startDistance();
        double remaining = Math.max(0, ctx.bulletObject.getRange() - ctx.bulletObject.getTraveled());
        double increment = Math.min(stepDistance(ctx), remaining + Math.max(0, freeEnd - before));
        if (p.suspended()) {
            p.recoveryStart(before);
            p.recoveryOffset(ctx.position.subtract(point(p, before)));
            Vec3 heading = ctx.velocity.lengthSqr() > 1e-12 ? ctx.velocity.normalize() : p.launchAxis();
            p.recoveryTangent(heading.subtract(tangent(p, before)));
            p.resume();
        }
        double after = before + increment;
        Vec3 next = point(p, after).add(recovery(p, after));
        Vec3 velocity =
            tangent(p, after).add(recoveryDerivative(p, after)).scale(ctx.baseVelocity.length());
        var path = new ArrayList<Vec3>();
        var pathCosts = new ArrayList<Double>();
        int count = Math.max(1, (int) Math.ceil(increment / 0.1));
        for (int i = 1; i <= count; i++) {
            double d = before + increment * i / count;
            path.add(point(p, d).add(recovery(p, d)));
            double freeBefore = Math.max(0, freeEnd - (before + increment * (i - 1) / count));
            pathCosts.add(Math.max(0, increment / count - freeBefore));
        }
        p.distance(after);
        ctx.runtimeState.stage(after < freeEnd ? 0 : 1);
        double charge = Math.min(remaining, Math.max(0, increment - Math.max(0, freeEnd - before)));
        return new TrajectoryMotion(
            next,
            velocity,
            Vec3.ZERO,
            velocity,
            increment / Math.max(1e-12, ctx.deltaTicks),
            TrajectoryMotion.ControlMode.INTENT,
            TrajectoryMotion.CompositionMode.PRIMARY,
            charge,
            path,
            pathCosts);
    }

    private static Vec3 point(TrajectoryPathState p, double distance) {
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

    private static Vec3 tangent(TrajectoryPathState p, double d) {
        if (d < TRANSITION)
            return p.launchAxis()
                .add(p.planeSide().scale(RADIUS / TRANSITION * smoothDerivative(d / TRANSITION)));
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
                .add(p.planeSide().scale(RADIUS / TRANSITION * smoothDerivative(1 - d / TRANSITION)));
        return p.launchAxis().scale(-1);
    }

    private static double smooth(double t) {
        return t * t * t * (10 + t * (-15 + 6 * t));
    }

    private static double smoothDerivative(double t) {
        return 30 * t * t * (1 - t) * (1 - t);
    }

    private static Vec3 recovery(TrajectoryPathState p, double distance) {
        double t = TrajectoryFrame.clamp((distance - p.recoveryStart()) / RECOVERY, 0, 1);
        return p.recoveryOffset()
            .scale(2 * t * t * t - 3 * t * t + 1)
            .add(p.recoveryTangent().scale(RECOVERY * (t * t * t - 2 * t * t + t)));
    }

    private static Vec3 recoveryDerivative(TrajectoryPathState p, double distance) {
        double t = TrajectoryFrame.clamp((distance - p.recoveryStart()) / RECOVERY, 0, 1);
        return p.recoveryOffset()
            .scale((6 * t * t - 6 * t) / RECOVERY)
            .add(p.recoveryTangent().scale(3 * t * t - 4 * t + 1));
    }
}
