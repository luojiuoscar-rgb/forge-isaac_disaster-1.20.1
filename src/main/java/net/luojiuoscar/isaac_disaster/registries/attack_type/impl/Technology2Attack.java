package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.AdditionalAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class Technology2Attack extends AbstractLaserAttack implements AdditionalAttackType {
    public static final float DAMAGE_PERCENTAGE = 0.1f;
    private static final double BASE_LASER_WIDTH = 0.25D;

    public Technology2Attack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public Technology2Attack(double priority) {
        super(priority);
    }

    @Override public ResourceLocation getId() { return ModAttackTypes.TECHNOLOGY2.getId(); }

    @Override
    public void performAttack(List<AttackContext> contexts) {
        performLaserBatch(contexts, ignored -> 0);
    }

    @Override
    protected double getWidth(AttackContext context) {
        return laserWidth(context.getBulletScale(), BASE_LASER_WIDTH);
    }

    @Override
    protected Vector3f getDefaultLaserColor() {
        return new Vector3f(1.0F, 0.0F, 0.0F);
    }

    @Override
    public void onTick(ServerPlayer player) {
        if (!PlayerHelper.isHoldingIsaacHead(player)) return;
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            if (!ability.isHoldingRightClick()) return;
            AttackExecutor.perform(AttackRequest.withContexts(player, this, AttackOrigin.ABILITY_EXTRA,
                    AttackPipelineMode.PREPARE_AND_EXECUTE, getAttackContexts(player, 1), false));
        });
    }

    @Override
    public void makeSound(LivingEntity entity) {
    }

    @Override
    protected boolean applyDamage(LivingEntity source, LivingEntity target, float damage) {
        return target.hurt(getDamageSource(source), damage * DAMAGE_PERCENTAGE);
    }

    @Override
    public @Nullable AttackContext createAttackContext(ServerPlayer player, Entity shooter) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(playerAbility -> {
                    ResourceLocation colorRl = playerAbility.getBestBulletColor();
                    Vec3 eyePos = player.getEyePosition().add(0, player.getBbHeight() * -0.15, 0);
                    // offset
                    Vec3 look = player.getLookAngle();
                    Vec3 up = new Vec3(0, 1, 0);
                    Vec3 left = look.cross(up).normalize();
                    eyePos = eyePos.add(left.scale(-0.5));

                    return AttackContext.builder(player, shooter).attackType(this)
                            .color(colorRl).trigger(new CompositeTrigger())
                            .position(eyePos).mainAxis(GeometryHelper.mainAxisFromRotation(player.getXRot(), player.getYRot()))
                            .range(getRange(player)).speed(getBulletSpeed(player)).build();
                })
                .orElse(null);
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        AttackContext context = createAttackContext(player, player);
        if (context == null) return List.of();
        return List.of(context);
    }
}
