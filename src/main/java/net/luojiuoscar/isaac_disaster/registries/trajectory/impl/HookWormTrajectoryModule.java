package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.OffsetTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryKinematics;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntimeState;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.world.phys.Vec3;

/** Stage 0 advances forward; stage 1 crosses to the opposite side. */
public final class HookWormTrajectoryModule extends OffsetTrajectoryModule {
    public static final double FORWARD_DISTANCE = 2;
    public static final double SIDE_AMPLITUDE = 1;

    public boolean lateral(TrajectoryRuntimeState s) {
        return s.stage() == 1;
    }

    @Override
    public boolean pausesDefaultMotion(TrajectoryRuntimeState s, boolean hasPrimary) {
        return !hasPrimary && lateral(s);
    }

    @Override
    public double boundaryAdvanceRate(
        TrajectoryRuntimeState s, double primaryRate, double movementRate) {
        return lateral(s) ? movementRate : primaryRate;
    }

    @Override
    public double distanceToBoundary(TrajectoryRuntimeState s) {
        return lateral(s)
            ? Math.abs(s.direction() * SIDE_AMPLITUDE - s.lateralOffset())
            : FORWARD_DISTANCE - s.phase();
    }

    @Override
    public void advance(TrajectoryRuntimeState s, double primaryDistance, double movementBudget) {
        if (lateral(s)) {
            double target = s.direction() * SIDE_AMPLITUDE;
            double delta = Math.min(Math.abs(target - s.lateralOffset()), movementBudget);
            s.lateralOffset(s.lateralOffset() + Math.copySign(delta, target - s.lateralOffset()));
            if (Math.abs(target - s.lateralOffset()) < 1e-9) {
                s.lateralOffset(target);
                s.stage(0);
                s.phase(0);
                s.direction(-s.direction());
            }
        } else {
            s.phase(s.phase() + primaryDistance);
            if (s.phase() >= FORWARD_DISTANCE - 1e-9) {
                s.phase(0);
                s.stage(1);
            }
        }
    }

    @Override
    public Vec3 offset(TrajectoryKinematics f, TrajectoryRuntimeState s, int amplifier) {
        return f.launchRight().scale(s.lateralOffset());
    }

    @Override
    public boolean freeDistanceCoveredByPrimary(TrajectoryRuntimeState state, boolean hasPrimary) {
        return hasPrimary;
    }

    @Override
    public double maximumFreeDistance(
        int amplifier, java.util.List<TrajectorySpec> specs, double range) {
        return Math.max(0, range) + 2;
    }
}
