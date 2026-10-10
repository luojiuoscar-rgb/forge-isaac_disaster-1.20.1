package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackExecutor;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.DelegatingAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class NeptunusAttack extends AttackType implements IChargeableAttack, DelegatingAttackType {

    public NeptunusAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public NeptunusAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.NEPTUNUS.getId();
    }

    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        return List.of();
    }

    @Override
    public void performAttack(List<AttackContext> ctxList) {}

    @Override
    public void makeSound(LivingEntity entity) {}

    @Override
    public void shoot(AttackContext ctx) {}

    private static final double[] RELEASE_DELAY_FACTORS =
            {1.0, 0.9, 0.8, 0.7, 0.6, 0.55, 0.5, 0.45, 0.4, 0.35, 0.3, 0.25, 0.25, 0.25, 0.25};
    private static final double[] CUMULATIVE_CHARGE_FACTORS = cumulativeFactors();
    private static final double TOTAL_CHARGE_FACTOR =
            CUMULATIVE_CHARGE_FACTORS[CUMULATIVE_CHARGE_FACTORS.length - 1];


    @Override
    public void onTick(ServerPlayer player) {

        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(playerAbility -> {

            double shotDelay = Math.max(1, getShotDelay(player));
            int chargeAmount = playerAbility.getChargeAmount(getChargeBarId());

            boolean streamMode = shotDelay <= 1;

            // 低射速的时候直接持续发射
            if (streamMode) {

                if (playerAbility.isHoldingRightClick()
                        && !player.getCooldowns().isOnCooldown(ModItems.ISAAC_HEAD.get())) {

                    AttackType attack =
                            playerAbility.getAttackSelection().baseAttack();

                    BeforePerformAttackEvent event =
                            new BeforePerformAttackEvent(player, this);

                    MinecraftForge.EVENT_BUS.post(event);
                    if (event.isCanceled()) return;

                    AttackExecutor.perform(AttackRequest.generated(
                            player, attack, AttackOrigin.PLAYER_PRIMARY,
                                AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false));
                    attack.makeSound(player);
                }

                return;
            }

            // 默认发射系统
            int totalCharge = getTotalCharge(player);

            if (playerAbility.isHoldingRightClick()
                    && chargeAmount > 2
                    && !player.getCooldowns().isOnCooldown(ModItems.ISAAC_HEAD.get())) {

                AttackType attack =
                        playerAbility.getAttackSelection().baseAttack();

                BeforePerformAttackEvent event =
                        new BeforePerformAttackEvent(player, this);

                MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) return;

                AttackExecutor.perform(AttackRequest.generated(
                        player, attack, AttackOrigin.PLAYER_PRIMARY,
                        AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE, false));

                attack.makeSound(player);

                int coolDownTick = getCoolDownTicks(shotDelay, chargeAmount);

                player.getCooldowns().addCooldown(ModItems.ISAAC_HEAD.get(), coolDownTick);
                playerAbility.setChargeAmount(getChargeBarId(), chargeAmount - coolDownTick);
            }

            // 充能逻辑
            else if (chargeAmount < totalCharge
                    && !playerAbility.isHoldingRightClick()) {

                addCharge(player, 1);
            }

            else if (chargeAmount > totalCharge) {
                playerAbility.setChargeAmount(getChargeBarId(), totalCharge);
            }
        });
    }

    private int getCoolDownTicks(double shotDelay, int chargeAmount) {

        shotDelay = Math.max(1, shotDelay);

        double normalizedCharge = Math.max(0, chargeAmount) / shotDelay;
        for (int i = 0; i < RELEASE_DELAY_FACTORS.length; i++) {
            if (normalizedCharge <= CUMULATIVE_CHARGE_FACTORS[i]) {
                return Math.max(1, (int) (shotDelay * RELEASE_DELAY_FACTORS[i]));
            }
        }

        return Math.max(1,
                (int) (shotDelay * RELEASE_DELAY_FACTORS[RELEASE_DELAY_FACTORS.length - 1]));
    }

    @Override
    public int getTotalCharge(Player player) {
        double shotDelay = Math.max(1, getShotDelay(player));
        return (int) (shotDelay * TOTAL_CHARGE_FACTOR);
    }

    private static double[] cumulativeFactors() {
        double[] cumulative = new double[RELEASE_DELAY_FACTORS.length];
        double total = 0;
        for (int i = 0; i < RELEASE_DELAY_FACTORS.length; i++) {
            total += RELEASE_DELAY_FACTORS[i];
            cumulative[i] = total;
        }
        return cumulative;
    }

    @Override
    public void onPressed(ServerPlayer player) {
    }

    @Override
    public void onReleased(ServerPlayer player) {
    }

    @Override
    public int getChargePerTick(ServerPlayer player) {
        if (getShotDelay(player) <= 1) return 0;
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.isHoldingRightClick() ? 0 : 1).orElse(0);
    }
}
