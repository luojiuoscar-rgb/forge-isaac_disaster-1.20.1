package net.luojiuoscar.isaac_disaster.entity.familiar.base.baby;

import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Stable automatic targeting, independent of the concrete attack and chain movement. */
public abstract class AbstractAutoAttackBabyFamiliarEntity extends AbstractBabyFamiliarEntity {
    private LivingEntity target;
    private long nextScanTick = Long.MIN_VALUE;

    protected AbstractAutoAttackBabyFamiliarEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    protected abstract double targetDetectionRange();

    protected boolean requiresTargetLineOfSight() { return true; }

    @Override
    protected final @Nullable Vec3 findAttackDirection(ServerPlayer owner) {
        if (owner.isSpectator()) {
            target = null;
            return null;
        }
        double configuredRange = targetDetectionRange();
        double range = Double.isFinite(configuredRange) ? Math.max(0, configuredRange) : 0;
        Vec3 origin = attackOrigin();
        boolean lostTarget = target != null && !validTarget(owner, target, origin, range);
        if (lostTarget) target = null;
        long now = level().getGameTime();
        if (target == null && (lostTarget || now >= nextScanTick)) {
            nextScanTick = now + 4;
            int bestPriority = Integer.MAX_VALUE;
            double bestDistance = Double.POSITIVE_INFINITY;
            for (LivingEntity candidate : level().getEntitiesOfClass(LivingEntity.class,
                    new AABB(origin, origin).inflate(range))) {
                if (!validTarget(owner, candidate, origin, range)) continue;
                int priority = TrackingTargetRules.priority(owner.serverLevel(), owner, candidate);
                double distance = origin.distanceToSqr(candidate.getBoundingBox().getCenter());
                if (priority < bestPriority || priority == bestPriority && (distance < bestDistance
                        || distance == bestDistance && (target == null || candidate.getId() < target.getId()))) {
                    target = candidate;
                    bestPriority = priority;
                    bestDistance = distance;
                }
            }
        }
        return target == null ? null : target.getBoundingBox().getCenter().subtract(origin);
    }

    private boolean validTarget(ServerPlayer owner, LivingEntity candidate, Vec3 origin, double range) {
        if (candidate.isRemoved() || candidate.level() != level()
                || !TrackingTargetRules.eligibleFlyOrSpider(owner.serverLevel(), owner, candidate)) return false;
        Vec3 center = candidate.getBoundingBox().getCenter();
        return origin.distanceToSqr(center) <= range * range && (!requiresTargetLineOfSight()
                || level().clip(new ClipContext(origin, center, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS);
    }

    @Override
    protected void resetBabyAction() {
        super.resetBabyAction();
        target = null;
        nextScanTick = Long.MIN_VALUE;
    }
}
