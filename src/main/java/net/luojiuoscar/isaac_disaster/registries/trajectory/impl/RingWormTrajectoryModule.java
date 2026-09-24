package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.trajectory.PeriodicOffsetTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryKinematics;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntimeState;
import net.minecraft.world.phys.Vec3;

public final class RingWormTrajectoryModule extends PeriodicOffsetTrajectoryModule {
    @Override
    public double amplitude(int amplifier) {
        return 1 + Math.max(0, amplifier) * 0.5;
    }

    @Override
    public Vec3 offset(TrajectoryKinematics f, TrajectoryRuntimeState s, int amplifier) {
        return f.launchRight()
            .scale(Math.cos(angle(s)))
            .add(f.launchUp().scale(Math.sin(angle(s))))
            .scale(amplitude(amplifier) * envelope(s));
    }
}
