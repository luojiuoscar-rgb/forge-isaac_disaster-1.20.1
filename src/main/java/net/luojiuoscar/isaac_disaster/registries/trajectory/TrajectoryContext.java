package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// ================================
// 轨迹上下文
// ================================
public class TrajectoryContext {
    public final Vec3 dir;
    public final double distance;
    public final double deltaDistance;
    public final LivingEntity owner;
    public final Vec3 pos;
    public final int amplifier;
    public final double startYRot;

    public TrajectoryContext(Vec3 dir, double distance, double deltaDistance,
                             @Nullable LivingEntity owner, Vec3 pos, int amplifier, double startYRot) {
        this.dir = dir;
        this.distance = distance;
        this.deltaDistance = deltaDistance;
        this.owner = owner;
        this.pos = pos;
        this.amplifier = Math.max(0, amplifier);
        this.startYRot = startYRot;
    }
}
