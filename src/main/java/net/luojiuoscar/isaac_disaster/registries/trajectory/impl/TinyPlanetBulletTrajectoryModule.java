package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

/** Ordinary projectile Tiny Planet behavior. */
public final class TinyPlanetBulletTrajectoryModule extends TinyPlanetTrajectoryModule {
    @Override
    protected boolean usesLaserPath() {
        return false;
    }
}
