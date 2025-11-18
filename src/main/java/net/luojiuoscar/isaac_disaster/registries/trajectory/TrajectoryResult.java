package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.world.phys.Vec3;

public record TrajectoryResult(
        Vec3 positionOffset,
        Vec3 velocityOffset,
        double speedDelta,
        double xRot,
        double yRot
) {
    public static final TrajectoryResult ZERO =
            new TrajectoryResult(Vec3.ZERO, Vec3.ZERO, 0, 0, 0);

    public TrajectoryResult(Vec3 positionOffset, Vec3 velocityOffset) {
        this(positionOffset, velocityOffset, 0, 0, 0);
    }
}
