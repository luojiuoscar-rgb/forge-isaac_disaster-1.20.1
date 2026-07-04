package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifier;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.server.level.ServerPlayer;

/**
 * Centralizes IsaacDisaster's access to vanilla flight flags.
 *
 * <p>Minecraft stores flight permission in shared player ability booleans, so this helper records
 * whether this mod changed {@code mayfly} and what the value was before that change. Removing Isaac
 * flight then restores this mod's own change without blindly disabling flight granted elsewhere.</p>
 */
public final class FlightHelper {
    private FlightHelper() {
    }

    /**
     * Returns whether IsaacDisaster currently grants this player a flight source.
     */
    public static boolean hasIsaacFlightSource(ServerPlayer player) {
        return hasIsaacFlightSource(player, true);
    }

    /**
     * Returns whether IsaacDisaster currently grants this player a flight source.
     *
     * @param includeTranscendence whether the Transcendence effect should count as an active source
     */
    public static boolean hasIsaacFlightSource(ServerPlayer player, boolean includeTranscendence) {
        return PlayerHelper.canFly(player)
                || (includeTranscendence && player.hasEffect(ModEffects.TRANSCENDENCE.get()));
    }

    /**
     * Ensures vanilla flight permission is enabled while remembering the previous permission state.
     */
    public static void grantIsaacFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(statModifier -> {
            if (!statModifier.isIsaacFlightGranted()) {
                statModifier.markIsaacFlightGranted(player.getAbilities().mayfly);
            }

            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        });
    }

    /**
     * Refreshes vanilla flight permission from the current Isaac flight sources.
     */
    public static void refreshIsaacFlight(ServerPlayer player) {
        refreshIsaacFlight(player, true);
    }

    /**
     * Refreshes vanilla flight permission from the current Isaac flight sources.
     *
     * @param includeTranscendence whether the Transcendence effect should count as an active source
     */
    public static void refreshIsaacFlight(ServerPlayer player, boolean includeTranscendence) {
        if (player.isCreative() || player.isSpectator()) return;

        if (hasIsaacFlightSource(player, includeTranscendence)) {
            grantIsaacFlight(player);
            return;
        }

        revokeIsaacFlight(player, true);
    }

    /**
     * Stops active flying only when IsaacDisaster is known to manage the player's flight.
     */
    public static void stopIsaacFlying(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) return;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(statModifier -> {
            if (!statModifier.isIsaacFlightGranted()) return;
            if (statModifier.wasMayflyBeforeIsaacFlight()) return;
            if (!player.getAbilities().flying) return;

            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        });
    }

    /**
     * Revokes IsaacDisaster's vanilla flight permission change.
     *
     * @param stopFlying whether the active flying state should also be cancelled
     */
    public static void revokeIsaacFlight(ServerPlayer player, boolean stopFlying) {
        if (player.isCreative() || player.isSpectator()) return;

        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(statModifier -> {
            if (!statModifier.isIsaacFlightGranted()) return;

            boolean restoreMayfly = statModifier.wasMayflyBeforeIsaacFlight();
            statModifier.clearIsaacFlightGrant();

            if (stopFlying && !restoreMayfly) {
                player.getAbilities().flying = false;
            }

            if (player.getAbilities().mayfly != restoreMayfly) {
                player.getAbilities().mayfly = restoreMayfly;
            }

            player.onUpdateAbilities();
        });
    }

    /**
     * Re-applies copied flight ownership state after a player entity clone.
     */
    public static void restoreCopiedFlightState(ServerPlayer player, PlayerStatModifier statModifier) {
        if (!statModifier.isIsaacFlightGranted()) return;

        if (hasIsaacFlightSource(player, true)) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
            return;
        }

        revokeIsaacFlight(player, true);
    }
}
