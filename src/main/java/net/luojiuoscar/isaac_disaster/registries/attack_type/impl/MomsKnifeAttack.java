package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.HeldMomsKnifeGroup;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MomsKnifeBarrage;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
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
        return isSelectedAndReady(player) && !HeldMomsKnifeGroup.isInFlight(player);
    }

    @Override
    public void onTick(ServerPlayer player) {
        if (!isChargeEligible(player)) {
            clearCharge(player);
            return;
        }
        if (!HeldMomsKnifeGroup.refresh(player).isEmpty()) addCharge(player, getChargePerTick(player));
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
        if (bulletCount <= 0) return List.of();
        int count = Mth.clamp(bulletCount, 1, HeldMomsKnifeGroup.MAX_KNIVES);
        Vec3 idle = PlayerMomsKnifeMotion.idlePosition(player, 1);
        AttackContext reference = AttackContext.builder(player, player).attackType(this)
                .position(idle).mainAxis(player.getLookAngle()).range(getRange(player)).speed(getBulletSpeed(player)).build();
        List<AttackContext> contexts = new ArrayList<>(count);
        for (int slot = 0; slot < count; slot++) {
            var layout = HeldMomsKnifeGroup.layout(count, slot, Math.max(0.6, player.getBbWidth()));
            contexts.add(reference.toBuilder().position(idle.add(PlayerMomsKnifeMotion.offsetToWorld(
                            layout.offset(), player.getYRot(), player.getXRot())))
                    .mainAxis(PlayerMomsKnifeMotion.toWorld(layout.direction(), player.getYRot(), player.getXRot())).build());
        }
        return contexts;
    }

    @Override
    public void performAttack(List<AttackContext> contexts) {
        if (contexts.isEmpty() || !(contexts.get(0).getOwner() instanceof ServerPlayer player) || !isChargeEligible(player)) return;
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            double fraction = Math.min(1, (double) Math.max(1, ability.getChargeAmount(getChargeBarId()))
                    / getTotalCharge(player));
            int barrageCount = ability.getAttackTypes().getOrDefault(ModAttackTypes.BRIMSTONE.getId(), 0) > 0
                    ? MomsKnifeBarrage.countForCharge(fraction) : 0;
            HeldMomsKnifeGroup.launch(player, contexts, fraction, barrageCount);
        });
    }

    @Override
    public void shoot(AttackContext context) {
        performAttack(List.of(context));
    }

    @Override
    public void makeSound(LivingEntity entity) {}
}
