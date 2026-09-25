package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModulePriority;

/** Laser projectile Tiny Planet behavior; retained as a distinct registry module. */
public final class TinyPlanetLaserTrajectoryModule
        extends TinyPlanetTrajectoryModule<TinyPlanetLaserTrajectoryModule.State> {
    public static final class State extends TinyPlanetTrajectoryModule.State {
        @Override
        public State copy() {
            State copy = new State();
            copyInto(copy);
            return copy;
        }
    }

    @Override
    protected State newState() {
        return new State();
    }

    @Override
    protected Class<State> stateClass() {
        return State.class;
    }

    @Override
    public int priority() {
        return TrajectoryModulePriority.TINY_PLANET_LASER.priority();
    }

    @Override
    protected boolean blocksFollowingPrimaryTyped(State state, int amplifier) {
        return state.distance() < maximumFreeDistance(amplifier) - 1.0E-9D;
    }

    @Override
    protected boolean usesLaserPath() {
        return true;
    }
}
