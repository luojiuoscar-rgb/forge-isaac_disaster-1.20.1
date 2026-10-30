package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.UranusAttachment;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.ModAttackPatterns;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectPlacement;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/** Freezes ordinary mobs while letting external motion slide across the ground. */
public final class FrozenEffect extends EntityFreezeEffect {
    private static final ResourceLocation SOURCE = ModEffects.FROZEN.getId();
    private static final ResourceLocation LAYER = ModVisualLayers.FROZEN.getId();
    private static final String SOURCE_UUID = IsaacDisaster.MOD_ID + ":frozen_source";
    private static final String ICE_STATUE = IsaacDisaster.MOD_ID + ":uranus_ice_statue";
    public static final int SHARD_COUNT = 12;

    public FrozenEffect(MobEffectCategory category, int color) {
        super(category, color, SOURCE, LAYER);
    }

    public static void begin(Mob mob, @Nullable Entity source) {
        if (source != null) mob.getPersistentData().putUUID(SOURCE_UUID, source.getUUID());
        else mob.getPersistentData().remove(SOURCE_UUID);
        leaveWater(mob);
    }

    @Nullable
    public static UUID sourceUuid(LivingEntity entity) {
        return entity.getPersistentData().hasUUID(SOURCE_UUID) ? entity.getPersistentData().getUUID(SOURCE_UUID) : null;
    }

    @Nullable
    public static ServerPlayer resolvePlayer(ServerLevel level, @Nullable UUID id) {
        return id != null && level.getEntity(id) instanceof ServerPlayer player
                && player.isAlive() && player.level() == level ? player : null;
    }

    public static void markIceStatue(Mob mob) { mob.getPersistentData().putBoolean(ICE_STATUE, true); }

    public static boolean isIceStatue(LivingEntity entity) { return entity.getPersistentData().getBoolean(ICE_STATUE); }

    public static void clearLifecycle(LivingEntity entity) {
        entity.getPersistentData().remove(SOURCE_UUID);
        entity.getPersistentData().remove(ICE_STATUE);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        // Amplifier upgrades also replace modifiers while the same effect remains active.
        if (!entity.hasEffect(this)) clearLifecycle(entity);
    }

    public static void tickFrozen(Mob mob) {
        if (!mob.isAlive() || !mob.hasEffect(ModEffects.FROZEN.get())) return;
        if (isIceStatue(mob) && mob.getHealth() > 1) mob.setHealth(1);
        if ((mob.tickCount & 1) == 0) leaveWater(mob);
    }

    private static void leaveWater(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        GroundEffectPlacement.projectToGround(level, mob.position()).ifPresent(position -> {
            var player = resolvePlayer(level, sourceUuid(mob));
            GroundEffectRuntime.INSTANCE.manager(level).spawn(ModGroundEffects.WATER.get(),
                    player == null ? null : player.getUUID(), position, 0.75, 80,
                    attackDamage(player == null ? mob : player));
        });
    }

    public static float attackDamage(LivingEntity owner) {
        var attribute = owner.getAttribute(Attributes.ATTACK_DAMAGE);
        double damage = attribute == null ? 1 : attribute.getValue();
        return (float) (Double.isFinite(damage) ? Math.min(Float.MAX_VALUE, Math.max(0, damage)) : 1);
    }

    /** Builds clean horizontal shards without running the owner's attack or attachment pipeline. */
    public static List<BulletState> createShards(LivingEntity owner, Vec3 origin) {
        var attack = AttackContext.builder(owner, owner).attackType(ModAttackTypes.BULLET.get())
                .position(origin).mainAxis(new Vec3(1, 0, 0)).damage((double) attackDamage(owner))
                .speed(GameplayConfig.DEFAULT_BULLET_SPEED.get()).range(GameplayConfig.DEFAULT_BULLET_RANGE.get())
                .bulletScaleModifier(GameplayConfig.DEFAULT_BULLET_SCALE.get())
                .bulletKnockback(GameplayConfig.DEFAULT_BULLET_KNOCKBACK.get())
                .color(ModBulletColors.URANUS.getId()).build();
        var preparation = new ExecutableEffectContext(owner);
        preparation.set(ContextKeys.EFFECT_ATTACHMENT_NODE, attack);
        new UranusAttachment().apply(preparation);
        return ModAttackPatterns.RING.get().generate(new AttackPatternContext(attack, SHARD_COUNT)).stream()
                .map(context -> BulletState.from(context).renderScale(context.getBulletScale())
                        .collisionWidth(context.getBulletScale() * 0.2).collisionHeight(context.getBulletScale() * 0.2)
                        .lifetime((int) Math.max(1, Math.min(200, context.getBulletRange() / context.getBulletSpeed())))
                        .build()).toList();
    }

    public static void reconcileVisualState(LivingEntity entity) {
        EntityFreezeEffect.reconcileVisualState(entity, ModEffects.FROZEN.get(), SOURCE, LAYER);
    }
}
