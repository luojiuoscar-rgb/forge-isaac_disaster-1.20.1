package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.world.phys.Vec3;

// ================================
// 轨迹上下文
// ================================
public class TrajectoryContext {
    public final IBulletObject bulletObject;
    public final double deltaDistance;
    public final int amplifier;
    public final Vec3 trajectoryPos;

    public TrajectoryContext(IBulletObject bulletObject,
                             double deltaDistance, int amplifier, Vec3 trajectoryPos) {
        this.bulletObject = bulletObject;
        this.deltaDistance = deltaDistance;
        this.amplifier = Math.max(0, amplifier);
        this.trajectoryPos = trajectoryPos;
    }
}
