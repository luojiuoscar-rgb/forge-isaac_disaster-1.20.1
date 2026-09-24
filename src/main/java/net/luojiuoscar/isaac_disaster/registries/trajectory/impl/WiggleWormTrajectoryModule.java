package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.PeriodicOffsetTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryKinematics;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntimeState;
import net.minecraft.world.phys.Vec3;

public final class WiggleWormTrajectoryModule extends PeriodicOffsetTrajectoryModule {
    @Override
    public double amplitude(int amplifier) {
        return 1 + Math.max(0, amplifier) * 0.5;
    }

    @Override
    protected double phasePeriod() {
        return 4;
    }

    @Override
    public Vec3 offset(TrajectoryKinematics f, TrajectoryRuntimeState s, int amplifier) {
        return f.launchRight().scale(Math.sin(angle(s)) * amplitude(amplifier) * envelope(s));
    }
}
