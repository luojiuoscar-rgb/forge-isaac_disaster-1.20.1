package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.OffsetTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryKinematics;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Rectangular lateral motion with an independently advancing phase. */
public final class HookWormTrajectoryModule
        extends OffsetTrajectoryModule<HookWormTrajectoryModule.State> {
    private static final double FORWARD_DISTANCE = 2;
    private static final double SIDE_AMPLITUDE = 1;

    public static final class State implements TrajectoryState {
        private double phase;
        private int stage;
        private double direction = 1;
        private double lateralOffset;

        public double phase() {
            return phase;
        }

        public void phase(double value) {
            phase = Double.isFinite(value) ? Math.max(0, value) : 0;
        }

        public int stage() {
            return stage;
        }

        public void stage(int value) {
            stage = value;
        }

        public double direction() {
            return direction;
        }

        public void direction(double value) {
            direction = value < 0 ? -1 : 1;
        }

        public double lateralOffset() {
            return lateralOffset;
        }

        public void lateralOffset(double value) {
            lateralOffset = Double.isFinite(value) ? value : 0;
        }

        @Override
        public State copy() {
            State copy = new State();
            copy.phase = phase;
            copy.stage = stage;
            copy.direction = direction;
            copy.lateralOffset = lateralOffset;
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
        buffer.writeVarInt(state.stage());
        buffer.writeDouble(state.direction());
        buffer.writeDouble(state.lateralOffset());
    }

    @Override
    public State readState(FriendlyByteBuf buffer) {
        State state = new State();
        state.phase(buffer.readDouble());
        state.stage(buffer.readVarInt());
        state.direction(buffer.readDouble());
        state.lateralOffset(buffer.readDouble());
        return state;
    }

    @Override
    protected boolean pausesDefaultMotionTyped(State state, boolean hasPrimary) {
        return state.stage() == 1 && !hasPrimary;
    }

    @Override
    protected double distanceToBoundaryTyped(State state) {
        return state.stage() == 1
                ? Math.abs(state.direction() * SIDE_AMPLITUDE - state.lateralOffset())
                : Math.max(0, FORWARD_DISTANCE - state.phase());
    }

    @Override
    protected double boundaryAdvanceRateTyped(State state, double primaryRate, double movementRate) {
        return state.stage() == 1 ? movementRate : primaryRate;
    }

    @Override
    protected void advanceTyped(State state, double primaryDistance, double movementBudget) {
        if (state.stage() == 1) {
            double target = state.direction() * SIDE_AMPLITUDE;
            double delta = Math.min(Math.abs(target - state.lateralOffset()), movementBudget);
            state.lateralOffset(
                    state.lateralOffset() + Math.copySign(delta, target - state.lateralOffset()));
            if (Math.abs(target - state.lateralOffset()) < 1e-9) {
                state.lateralOffset(target);
                state.stage(0);
                state.phase(0);
                state.direction(-state.direction());
            }
        } else {
            state.phase(state.phase() + Math.max(0, primaryDistance));
            if (state.phase() >= FORWARD_DISTANCE - 1e-9) {
                state.phase(0);
                state.stage(1);
            }
        }
    }

    @Override
    protected Vec3 offsetTyped(TrajectoryKinematics frame, State state, int amplifier) {
        return frame.launchRight().scale(state.lateralOffset());
    }

    @Override
    protected int stageTyped(State state) {
        return state.stage();
    }

    @Override
    protected boolean freeDistanceCoveredByPrimaryTyped(State state, boolean hasPrimary) {
        return hasPrimary;
    }

    @Override
    public double maximumFreeDistance(
            int amplifier,
            List<TrajectorySpec>
                    specs,
            double range) {
        return Math.max(0, range) + FORWARD_DISTANCE;
    }
}
