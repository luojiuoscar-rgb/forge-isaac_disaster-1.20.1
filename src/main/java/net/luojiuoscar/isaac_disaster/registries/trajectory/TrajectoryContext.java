package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.manager.attack.IBulletObject;

// ================================
// 轨迹上下文
// ================================
public class TrajectoryContext {
    public final IBulletObject bulletObject;
    public final double deltaDistance;
    public final int amplifier;

    public TrajectoryContext(IBulletObject bulletObject, double deltaDistance, int amplifier) {
        this.bulletObject = bulletObject;
        this.deltaDistance = deltaDistance;
        this.amplifier = Math.max(0, amplifier);
    }
}
