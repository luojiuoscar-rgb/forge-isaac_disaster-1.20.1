package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.minecraft.world.phys.Vec3;

/** A returning controller samples the complete local blade, including its flight clock and facing. */
public interface ReturningKnifeTrajectory {
    double duration(double straightDuration);

    boolean suppressesHoming();

    int sweepSegments(double fromAge, double toAge, double duration);

    PlayerMomsKnifeMotion.Blade sampleLocal(Vec3 heldPosition, Vec3 direction,
                                           double range, double duration, double age);
}
