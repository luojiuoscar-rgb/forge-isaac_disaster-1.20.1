package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.HeldMomsKnifeGroup;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.HeldMomsKnifeAttackPlan;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPlan;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.BasicAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;

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
        return HeldMomsKnifeGroup.createBaseContexts(player, bulletCount);
    }

    @Override
    public double getChargeFraction(ServerPlayer player) {
        int total = getTotalCharge(player);
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> Math.min(1, (double) Math.max(1, ability.getChargeAmount(getChargeBarId())) / total))
                .orElse(0.0);
    }

    @Override
    public AttackPlan createPlan(AttackRequest request, List<AttackContext> contexts) {
        return new HeldMomsKnifeAttackPlan(request, contexts);
    }

    @Override
    public void performAttack(AttackPlan plan, List<AttackContext> contexts, Consumer<AttackContext> onFired) {
        if (!(plan instanceof HeldMomsKnifeAttackPlan knifePlan)) {
            throw new IllegalArgumentException("Mom's Knife requires its held knife plan");
        }
        launch(plan.getRequest().getPlayer(), knifePlan.getQueue(), contexts, onFired);
    }

    @Override
    public void performAttack(List<AttackContext> contexts, Consumer<AttackContext> onFired) {
        if (contexts.isEmpty() || !(contexts.get(0).getOwner() instanceof ServerPlayer player) || !isChargeEligible(player)) return;
        var queue = HeldMomsKnifeGroup.select(player, contexts, List.of());
        List<AttackContext> selected = queue.stream().map(entry -> entry.context().bindAttackTypeOrCopy(this)).toList();
        selected.forEach(AttackContext::freeze);
        launch(player, queue, selected, onFired);
    }

    private void launch(ServerPlayer player, List<HeldMomsKnifeGroup.QueuedKnife> queue,
                        List<AttackContext> contexts, Consumer<AttackContext> onFired) {
        if (player == null || contexts.isEmpty() || !isChargeEligible(player)) return;
        double charge = getChargeFraction(player);
        boolean triggerBarrage = player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getAttackTypes().getOrDefault(ModAttackTypes.BRIMSTONE.getId(), 0) > 0).orElse(false);
        AttackContext first = HeldMomsKnifeGroup.launch(player, queue, contexts, charge, triggerBarrage);
        if (first != null) onFired.accept(first);
    }

    @Override
    public void shoot(AttackContext context) {
        performAttack(List.of(context));
    }

    @Override
    public void makeSound(LivingEntity entity) {}
}
