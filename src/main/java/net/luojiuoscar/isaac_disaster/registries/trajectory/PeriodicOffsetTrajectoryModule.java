package net.luojiuoscar.isaac_disaster.registries.trajectory;

/**
 * Shared base for offset trajectories whose phase advances continuously and is evaluated as an
 * angle.
 */
public abstract class PeriodicOffsetTrajectoryModule extends OffsetTrajectoryModule {
    protected static final double ENTRY_DISTANCE = 2;
    protected static final double PERIOD = 8;

    /** Smoothly enables the periodic offset during the initial entry distance. */
    protected final double envelope(TrajectoryRuntimeState state) {
        return TrajectoryFrame.smootherstep(state.phase() / ENTRY_DISTANCE);
    }

    /** Converts the distance based phase into the current periodic angle. */
    protected final double angle(TrajectoryRuntimeState state) {
        return state.phase() * (2 * Math.PI / phasePeriod());
    }

    /** Distance travelled by the primary path for one complete cycle. */
    protected double phasePeriod() {
        return PERIOD;
    }
}
