package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
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

    public Technology2Attack(double priority) {
        super(priority);
    }

    @Override
    public void makeSound(LivingEntity entity) {
    }

    @Override
    protected LivingEntity makeDamage(LivingEntity source, LivingEntity target, float damage) {
        target.invulnerableTime = 0;
        target.hurt(getDamageSource(source), damage * DAMAGE_PERCENTAGE);
        return target;
    }

    @Override
    @Nullable
    public AttackContext getOneAttackContext(ServerPlayer player, Entity shooter) {
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

                    return new AttackContext(
                            player,
                            shooter,
                            colorRl,
                            new TriggerModuleQueue(),
                            trajectories,
                            eyePos,
                            player.getXRot(),
                            player.getYRot());
                })
                .orElse(new AttackContext());
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        AttackContext context = getOneAttackContext(player, player);
        return context != null ? List.of(context) : List.of();
    }
}
