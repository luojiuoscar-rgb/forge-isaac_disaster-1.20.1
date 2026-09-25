package net.luojiuoscar.isaac_disaster.registries.trajectory;

/**
 * Shared base for offset trajectories whose phase advances continuously and is evaluated as an
 * angle.
 */
public abstract class PeriodicOffsetTrajectoryModule<S extends TrajectoryState>
        extends OffsetTrajectoryModule<S> {
    protected static final double ENTRY_DISTANCE = 2;
    protected static final double PERIOD = 8;

    protected final double envelope(S state) {
        return TrajectoryFrame.smootherstep(phase(state) / ENTRY_DISTANCE);
    }

    protected final double angle(S state) {
        return phase(state) * (2 * Math.PI / phasePeriod());
    }

    @Override
    public double telemetryPhase(TrajectoryState state) {
        return phase(castState(state));
    }

    @Override
    protected void advanceTyped(S state, double primaryDistance, double movementBudget) {
        setPhase(state, phase(state) + Math.max(0.0D, primaryDistance));
    }

    /** Module-specific phase, kept out of the shared runtime state. */
    protected abstract double phase(S state);

    protected abstract void setPhase(S state, double value);

    protected double phasePeriod() {
        return PERIOD;
    }
}
