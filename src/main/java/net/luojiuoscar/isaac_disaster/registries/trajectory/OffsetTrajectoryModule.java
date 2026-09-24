package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.world.phys.Vec3;

/** Shape-only modules run after primary motion and cannot replace its velocity or range cost. */
public abstract class OffsetTrajectoryModule extends TrajectoryModule {
    @Override
    public final Role role() {
        return Role.OFFSET;
    }

    @Override
    public final TrajectoryMotion apply(TrajectoryContext context) {
        throw new IllegalStateException("Offset modules require the shared primary frame");
    }

    public abstract Vec3 offset(
        TrajectoryKinematics frame, TrajectoryRuntimeState state, int amplifier);

    public void advance(TrajectoryRuntimeState state, double primaryDistance, double movementBudget) {
        state.phase(state.phase() + primaryDistance);
    }

    public double amplitude(int amplifier) {
        return 0;
    }

    /** Whether this shape temporarily owns movement when no primary controller is active. */
    public boolean pausesDefaultMotion(TrajectoryRuntimeState state, boolean hasPrimary) {
        return false;
    }

    /** Remaining movement in this module before a discontinuous stage boundary. */
    public double distanceToBoundary(TrajectoryRuntimeState state) {
        return Double.POSITIVE_INFINITY;
    }

    /** Rate used to consume distanceToBoundary during this evaluator step. */
    public double boundaryAdvanceRate(
        TrajectoryRuntimeState state, double primaryRate, double movementRate) {
        return primaryRate;
    }

    /** Maximum primary-distance increment used for smooth curve sampling. */
    public double maxPhaseAdvance() {
        return 0.25;
    }
}
