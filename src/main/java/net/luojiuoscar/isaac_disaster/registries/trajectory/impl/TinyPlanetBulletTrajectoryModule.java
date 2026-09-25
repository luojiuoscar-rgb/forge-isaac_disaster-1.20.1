package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

/** Ordinary projectile Tiny Planet behavior. */
public final class TinyPlanetBulletTrajectoryModule
        extends TinyPlanetTrajectoryModule<TinyPlanetBulletTrajectoryModule.State> {
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
}
