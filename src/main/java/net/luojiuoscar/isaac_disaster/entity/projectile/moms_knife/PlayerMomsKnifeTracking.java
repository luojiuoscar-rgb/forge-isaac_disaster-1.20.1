package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Reuses ordinary tear eligibility without importing its speed or lifetime behavior. */
final class PlayerMomsKnifeTracking {
    private static final double RANGE_SQUARED = 16;
    @Nullable private LivingEntity target;
    private long nextSearchTick;

    @Nullable
    Vec3 targetPoint(ServerPlayer owner, Vec3 position, Vec3 direction, long now) {
        ServerLevel level = owner.serverLevel();
        if (target != null && !valid(level, owner, target, position)) target = null;
        if (target == null && now >= nextSearchTick) {
            nextSearchTick = now + 4;
            Vec3 searchCenter = position.add(PlayerMomsKnifeMotion.unit(direction).scale(2));
            int bestPriority = Integer.MAX_VALUE;
            double bestDistance = Double.POSITIVE_INFINITY;
            for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(position, position).inflate(4), entity -> valid(level, owner, entity, position))) {
                int priority = TrackingTargetRules.priority(level, owner, candidate);
                double distance = candidate.distanceToSqr(searchCenter);
                if (priority < bestPriority || priority == bestPriority && distance < bestDistance) {
                    target = candidate;
                    bestPriority = priority;
                    bestDistance = distance;
                }
            }
        }
        return target == null ? null : target.position().add(0, target.getBbHeight() * 0.5, 0);
    }

    private static boolean valid(ServerLevel level, ServerPlayer owner, LivingEntity candidate, Vec3 position) {
        return candidate.level() == level && !candidate.isRemoved() && !candidate.isSpectator()
                && candidate.distanceToSqr(position) <= RANGE_SQUARED
                && TrackingTargetRules.eligible(level, owner, candidate);
    }

    void clear() {
        target = null;
        nextSearchTick = 0;
    }
}
