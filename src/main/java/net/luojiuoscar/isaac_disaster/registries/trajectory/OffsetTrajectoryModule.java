package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.world.phys.Vec3;

/** Shape-only modules run after primary motion and cannot replace its velocity or range cost. */
public abstract class OffsetTrajectoryModule<S extends TrajectoryState>
        extends TrajectoryModule<S> {
    @Override
    public final Role role() {
        return Role.OFFSET;
    }

    @Override
    protected final TrajectoryMotion applyTyped(TrajectoryContext context, S state) {
        throw new IllegalStateException("Offset modules require the shared primary frame");
    }

    public final Vec3 offset(TrajectoryKinematics frame, TrajectoryState state, int amplifier) {
        return offsetTyped(frame, castState(state), amplifier);
    }

    protected abstract Vec3 offsetTyped(TrajectoryKinematics frame, S state, int amplifier);

    public final void advance(
            TrajectoryState state, double primaryDistance, double movementBudget) {
        advanceTyped(castState(state), primaryDistance, movementBudget);
    }

    protected void advanceTyped(S state, double primaryDistance, double movementBudget) {}

    public double amplitude(int amplifier) {
        return 0;
    }

    public final boolean pausesDefaultMotion(TrajectoryState state, boolean hasPrimary) {
        return pausesDefaultMotionTyped(castState(state), hasPrimary);
    }

    protected boolean pausesDefaultMotionTyped(S state, boolean hasPrimary) {
        return false;
    }

    public final double distanceToBoundary(TrajectoryState state) {
        return distanceToBoundaryTyped(castState(state));
    }

    protected double distanceToBoundaryTyped(S state) {
        return Double.POSITIVE_INFINITY;
    }

    public final double boundaryAdvanceRate(
            TrajectoryState state, double primaryRate, double movementRate) {
        return boundaryAdvanceRateTyped(castState(state), primaryRate, movementRate);
    }

    protected double boundaryAdvanceRateTyped(S state, double primaryRate, double movementRate) {
        return primaryRate;
    }

    /** Optional discrete stage used to advance across zero-distance boundaries. */
    public final int stage(TrajectoryState state) {
        return stageTyped(castState(state));
    }

    protected int stageTyped(S state) {
        return 0;
    }

    /** Maximum primary-distance increment used for smooth curve sampling. */
    public double maxPhaseAdvance() {
        return 0.25;
    }
}
