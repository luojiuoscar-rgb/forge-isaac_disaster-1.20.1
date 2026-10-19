package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Immutable event snapshot; the target UUID and position remain usable after target removal. */
public record BulletStickContext(BulletState bullet, @Nullable LivingEntity target,
                                 @Nullable UUID targetUuid, Vec3 position,
                                 int elapsedTicks, int durationTicks,
                                 @Nullable BulletStickEndReason endReason) {
    public BulletStickContext {
        Objects.requireNonNull(bullet, "bullet");
        Objects.requireNonNull(position, "position");
    }
}
