package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.PeriodicOffsetTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryKinematics;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Symmetric sinusoidal lateral motion. */
public final class WiggleWormTrajectoryModule extends PeriodicOffsetTrajectoryModule<WiggleWormTrajectoryModule.State> {

    public static final class State implements TrajectoryState {
        private double phase;

        public double phase() {
            return phase;
        }

        public void phase(double value) {
            phase = Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D;
        }

        @Override
        public State copy() {
            State copy = new State();
            copy.phase = phase;
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
        buffer.writeDouble(state.phase());
    }

    @Override
    public State readState(FriendlyByteBuf buffer) {
        State state = new State();
        state.phase(buffer.readDouble());
        return state;
    }

    @Override
    protected double phase(State state) {
        return state.phase();
    }

    @Override
    protected void setPhase(State state, double value) {
        state.phase(value);
    }

    @Override
    public double amplitude(int amplifier) {
        return 1 + Math.max(0, amplifier) * 0.5;
    }

    @Override
    protected double phasePeriod() {
        return 4;
    }

    @Override
    protected Vec3 offsetTyped(TrajectoryKinematics frame, State state, int amplifier) {
        return frame.launchRight()
                .scale(Math.sin(angle(state)) * amplitude(amplifier) * envelope(state));
    }
}
