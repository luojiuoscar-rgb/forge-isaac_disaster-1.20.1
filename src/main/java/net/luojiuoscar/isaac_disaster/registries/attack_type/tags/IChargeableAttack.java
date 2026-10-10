package net.luojiuoscar.isaac_disaster.registries.attack_type.tags;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.networking.ChargeBarSync;
import net.luojiuoscar.isaac_disaster.registries.charge_bar.ModChargeBars;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IChargeableAttack {

    default ResourceLocation getChargeBarId() {
        return ModChargeBars.ATTACK_CHARGE.getId();
    }

    default boolean isChargeEligible(ServerPlayer player) {
        return player.isAlive() && !player.isRemoved() && !player.isSpectator()
                && PlayerHelper.isHoldingIsaacHead(player)
                && !player.hasEffect(ModEffects.LACRIMAL_HYPOSECRETION.get());
    }

    default void onPressed(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                ability -> ability.setChargeAmount(getChargeBarId(), 0));
    }

    default void onReleased(ServerPlayer player) {
        clearCharge(player);
    }

    int getTotalCharge(Player player);

    /** Amount gained on the next tick; custom/manual sources may return zero. */
    default int getChargePerTick(ServerPlayer player) {
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.isHoldingRightClick() ? 1 : 0).orElse(0);
    }

    /** Adds charge within the source's bounds; the attack decides when to call this method. */
    default void addCharge(ServerPlayer player, int amount) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            int total = Math.max(1, getTotalCharge(player));
            ability.setChargeAmount(getChargeBarId(), (int) Math.max(0, Math.min(total,
                    (long) ability.getChargeAmount(getChargeBarId()) + amount)));
        });
    }

    default void syncCharge(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            int total = Math.max(1, getTotalCharge(player));
            int amount = ability.getChargeAmount(getChargeBarId());
            float rate = amount >= total ? 0f : (float) getChargePerTick(player) / total;
            ChargeBarSync.syncToPlayer(getChargeBarId(), true, (float) amount / total, rate, player);
        });
    }

    default void clearCharge(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                ability -> ability.clearChargeAmount(getChargeBarId()));
        ChargeBarSync.syncToPlayer(getChargeBarId(), false, 0f, player);
    }
}
