package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl;

import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColorSelection;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectPlacement;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AquariusPuddleEffect extends AbstractGroundEffect implements EffectAttachmentNode {
    public static final double RADIUS = WaterPuddleEffect.DEFAULT_RADIUS;
    public static final int LIFETIME = 100;
    public static final double SPEED = 0.25D;
    public static final double TRACKING_RANGE = 8.0D;
    private static final int SEARCH_INTERVAL = 4;
    private final CompositeTrigger statuses = new CompositeTrigger();
    private final BulletColorSelection colors = new BulletColorSelection();
    private BulletColorSelection.Result resolvedColor;
    private boolean homing;
    @Nullable
    private LivingEntity trackingTarget;
    private int targetSearchCooldown;

    public AquariusPuddleEffect(GroundEffectType<AquariusPuddleEffect> type, int slot, int generation,
                                @Nullable UUID owner, Vec3 position, double radius, int lifetime,
                                float damage, long order, int animationDurationTicks) {
        super(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks);
    }

    public void setHoming(boolean value) {
        homing = value;
        if (!value) {
            trackingTarget = null;
            targetSearchCooldown = 0;
        }
    }
    public boolean isHoming() { return homing; }

    @Override
    public boolean canRepaint() { return false; }

    @Override
    public void offerColor(ResourceLocation value) {
        if (colors.offer(value)) {
            resolvedColor = null;
            markDirty();
        }
    }

    @Override
    public int getColor() {
        if (resolvedColor == null) resolvedColor = colors.resolveForGroundEffect(0x3C9BE8);
        return resolvedColor.color();
    }

    @Override
    public void addStatusEffect(HitPhase phase, Supplier<? extends IExecutableEffect> effect) {
        if (statuses.contains(effect.get())) return;
        statuses.add(new SimpleTrigger(phase == HitPhase.BEFORE_DAMAGE
                ? ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE : ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, effect));
    }

    @Override
    public void onContactDamage(GroundEffectContact contact) {
        LivingEntity owner = contact.owner();
        LivingEntity target = contact.target();
        if (owner == null || !owner.isAlive() || owner.level() != contact.level()
                || !target.isAlive() || target.isInvulnerable() || damage() <= 0) return;
        if (!target.hurt(ModDamageSources.water(contact.level(), owner), damage())) return;
        var context = new ExecutableEffectContext(owner);
        context.set(ContextKeys.AMPLIFIER, 1.0);
        context.set(ContextKeys.SECONDARY_LIVING_ENTITIES, List.of(target));
        context.set(ContextKeys.TARGET_POSITION, target.position());
        statuses.fire(context, ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE);
        statuses.fire(context, ModTriggerTypes.BULLET_HIT_ENTITY_AFTER);
    }

    @Override
    @Nullable
    public Vec3 nextPosition(ServerLevel level, Function<AABB, List<LivingEntity>> trackingCandidates) {
        if (!homing) return position();
        if (!GroundEffectPlacement.hasSupport(level, position())) return null;
        if (ownerUuid() == null || !(level.getEntity(ownerUuid()) instanceof LivingEntity owner)
                || !owner.isAlive() || owner.level() != level) {
            trackingTarget = null;
            return position();
        }
        if (targetSearchCooldown > 0) targetSearchCooldown--;
        if (!validTrackingTarget(level, owner, trackingTarget)) trackingTarget = null;
        if (trackingTarget == null && targetSearchCooldown == 0) {
            AABB bounds = new AABB(position(), position()).inflate(TRACKING_RANGE, 0.51, TRACKING_RANGE);
            trackingTarget = selectTarget(level, owner, trackingCandidates.apply(bounds));
            targetSearchCooldown = SEARCH_INTERVAL;
        }
        if (trackingTarget == null) return position();
        Vec3 next = horizontalStep(position(), trackingTarget.position());
        if (next.equals(position())) return next;
        BlockPos fromBlock = BlockPos.containing(position());
        BlockPos nextBlock = BlockPos.containing(next);
        if (!level.hasChunksAt(new BlockPos(Math.min(fromBlock.getX(), nextBlock.getX()), fromBlock.getY(),
                Math.min(fromBlock.getZ(), nextBlock.getZ())),
                new BlockPos(Math.max(fromBlock.getX(), nextBlock.getX()), fromBlock.getY(),
                        Math.max(fromBlock.getZ(), nextBlock.getZ())))) return null;
        var wall = level.clip(new ClipContext(position().add(0, 0.05, 0), next.add(0, 0.05, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        // A ray ending exactly on a face can miss it; also test the destination above the support.
        AABB destination = new AABB(next.x - 0.0001D, next.y + 0.01D, next.z - 0.0001D,
                next.x + 0.0001D, next.y + 0.1D, next.z + 0.0001D);
        if (wall.getType() != HitResult.Type.MISS
                || level.getBlockCollisions(owner, destination).iterator().hasNext()) return position();
        return GroundEffectPlacement.hasContinuousSupport(level, position(), next) ? next : null;
    }

    private boolean validTrackingTarget(ServerLevel level, LivingEntity owner, @Nullable LivingEntity candidate) {
        if (candidate == null || candidate.hasEffect(ModEffects.FROZEN.get()) || candidate.isRemoved() || candidate.level() != level || !candidate.onGround()
                || Math.abs(candidate.getY() - position().y) > 0.51) return false;
        double dx = candidate.getX() - position().x;
        double dz = candidate.getZ() - position().z;
        return dx * dx + dz * dz <= TRACKING_RANGE * TRACKING_RANGE
                && TrackingTargetRules.eligible(level, owner, candidate);
    }

    @Nullable
    private LivingEntity selectTarget(ServerLevel level, LivingEntity owner, List<LivingEntity> candidates) {
        LivingEntity target = null;
        int bestPriority = Integer.MAX_VALUE;
        double bestDistance = TRACKING_RANGE * TRACKING_RANGE;
        for (LivingEntity candidate : candidates) {
            if (!validTrackingTarget(level, owner, candidate)) continue;
            double dx = candidate.getX() - position().x;
            double dz = candidate.getZ() - position().z;
            double distance = dx * dx + dz * dz;
            int priority = TrackingTargetRules.priority(level, owner, candidate);
            if (target == null || priority < bestPriority || priority == bestPriority && distance < bestDistance) {
                target = candidate;
                bestPriority = priority;
                bestDistance = distance;
            }
        }
        return target;
    }

    public static Vec3 horizontalStep(Vec3 from, Vec3 target) {
        double dx = target.x - from.x;
        double dz = target.z - from.z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 1.0E-8) return from;
        double scale = Math.min(SPEED, distance) / distance;
        return new Vec3(from.x + dx * scale, from.y, from.z + dz * scale);
    }
}
