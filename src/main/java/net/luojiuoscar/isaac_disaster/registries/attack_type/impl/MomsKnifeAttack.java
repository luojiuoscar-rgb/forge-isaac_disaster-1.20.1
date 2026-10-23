package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.BasicAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MomsKnifeAttack extends AttackType implements BasicAttackType, IChargeableAttack {
    public MomsKnifeAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    @Override
    public ResourceLocation getId() { return ModAttackTypes.MOMS_KNIFE.getId(); }

    public static boolean isSelectedAndReady(ServerPlayer player) {
        return player.isAlive() && !player.isRemoved() && !player.isSpectator()
                && PlayerHelper.isHoldingIsaacHead(player)
                && !player.hasEffect(ModEffects.LACRIMAL_HYPOSECRETION.get())
                && player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getAttackSelection().mainAttack() == ModAttackTypes.MOMS_KNIFE.get())
                .orElse(false);
    }

    @Override
    public boolean isChargeEligible(ServerPlayer player) {
        if (!isSelectedAndReady(player)) return false;
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).map(ability -> {
            PlayerMomsKnifeEntity knife = ability.getRuntimeEntities()
                    .getEntity(getId(), player.serverLevel(), PlayerMomsKnifeEntity.class);
            return knife == null || knife.getOwner() != player || !knife.isInFlight();
        }).orElse(false);
    }

    @Nullable
    private PlayerMomsKnifeEntity getOrCreateKnife(ServerPlayer player) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).map(ability -> {
            var entities = ability.getRuntimeEntities();
            PlayerMomsKnifeEntity knife = entities.getEntity(getId(), player.serverLevel(), PlayerMomsKnifeEntity.class);
            if (knife != null && knife.getOwner() != player) {
                entities.unregister(getId(), knife.getUUID());
                knife = null;
            }
            if (knife == null && isSelectedAndReady(player)) {
                knife = new PlayerMomsKnifeEntity(ModEntities.PLAYER_MOMS_KNIFE.get(), player.level());
                knife.initialize(player);
                if (!player.serverLevel().addFreshEntity(knife)) return null;
                entities.register(getId(), knife.getUUID());
            }
            return knife;
        }).orElse(null);
    }

    @Override
    public void onTick(ServerPlayer player) {
        if (getOrCreateKnife(player) != null) addCharge(player, getChargePerTick(player));
    }

    @Override
    public int getTotalCharge(Player player) {
        return Math.max(1, (int) Math.ceil(4 * getShotDelay(player)));
    }

    @Override
    public void onReleased(ServerPlayer player) {
        try {
            if (!isChargeEligible(player)) return;
            AttackExecutor.perform(AttackRequest.generated(player, this, AttackOrigin.PLAYER_PRIMARY,
                    AttackPipelineMode.FULL, false));
        } finally {
            clearCharge(player);
        }
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        return List.of(AttackContext.builder(player, player).attackType(this)
                .position(PlayerMomsKnifeMotion.idlePosition(player, 1)).mainAxis(player.getLookAngle())
                .range(getRange(player)).speed(getBulletSpeed(player)).build());
    }

    @Override
    public void performAttack(List<AttackContext> contexts) {
        if (!contexts.isEmpty()) shoot(contexts.get(0));
    }

    @Override
    public void shoot(AttackContext context) {
        if (!(context.getOwner() instanceof ServerPlayer player) || !isChargeEligible(player)) return;
        PlayerMomsKnifeEntity knife = getOrCreateKnife(player);
        if (knife == null) return;
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            double fraction = Math.min(1, (double) Math.max(1, ability.getChargeAmount(getChargeBarId()))
                    / getTotalCharge(player));
            knife.launch(PlayerMomsKnifeMotion.range(context.getBulletRange(), fraction), context.getBulletSpeed());
        });
    }

    @Override
    public void makeSound(LivingEntity entity) {}
}
