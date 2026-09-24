package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModulePriority;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntimeState;

/** Laser projectile Tiny Planet behavior; retained as a distinct registry module. */
public final class TinyPlanetLaserTrajectoryModule extends TinyPlanetTrajectoryModule {
    @Override
    public int priority() {
        return TrajectoryModulePriority.TINY_PLANET_LASER.priority();
    }

    @Override
    public boolean blocksFollowingPrimary(TrajectoryRuntimeState state, int amplifier) {
        return state.path().distance() < maximumFreeDistance(amplifier) - 1.0E-9D;
    }

    @Override
    protected boolean usesLaserPath() {
        return true;
    }
}
