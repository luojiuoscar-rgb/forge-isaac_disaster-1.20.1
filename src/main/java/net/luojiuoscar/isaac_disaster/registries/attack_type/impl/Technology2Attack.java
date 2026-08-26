package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Technology2Attack extends LaserAttack {
    public static final float DAMAGE_PERCENTAGE = 0.1f;

    public Technology2Attack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public Technology2Attack(double priority) {
        super(priority);
    }

    @Override
    public void makeSound(LivingEntity entity) {
    }

    @Override
    protected boolean makeDamage(LivingEntity source, LivingEntity target, float damage) {
        target.invulnerableTime = 0;
        return target.hurt(getDamageSource(source), damage * DAMAGE_PERCENTAGE);
    }

    @Override
    public @Nullable AttackContext createAttackContext(ServerPlayer player, Entity shooter) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(playerAbility -> {
                    ResourceLocation colorRl = playerAbility.getBestBulletColor();
                    Map<ResourceLocation, Integer> trajectories = playerAbility.getTrajectories();
                    Vec3 eyePos = player.getEyePosition().add(0, player.getBbHeight() * -0.15, 0);
                    // offset
                    Vec3 look = player.getLookAngle();
                    Vec3 up = new Vec3(0, 1, 0);
                    Vec3 left = look.cross(up).normalize();
                    eyePos = eyePos.add(left.scale(-0.5));

                    return AttackContext.builder(player, shooter)
                            .color(colorRl).trigger(new CompositeTrigger()).trajectories(trajectories)
                            .position(eyePos).rotation(player.getXRot(), player.getYRot())
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
