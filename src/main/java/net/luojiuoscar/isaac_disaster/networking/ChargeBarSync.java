package net.luojiuoscar.isaac_disaster.networking;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Validates charge updates before constructing a packet for the shared network sender. */
public final class ChargeBarSync {
    private static final int CORRECTION_INTERVAL = 10;

    public record State(float progress, float rate, long tick) {
    }
    private ChargeBarSync() {
    }

    public static boolean syncToPlayer(ResourceLocation id, boolean visible, float progress, ServerPlayer player) {
        return syncToPlayer(id, visible, progress, 0f, player);
    }

    /** Rate is normalized progress per server tick; zero supports direct/manual updates. */
    public static boolean syncToPlayer(ResourceLocation id, boolean visible, float progress,
            float rate, ServerPlayer player) {
        if (id == null || player == null) {
            IsaacDisaster.LOGGER.warn("Skipping charge bar sync with missing registry ID or recipient: {}", id);
            return false;
        }
        float normalized = Float.isFinite(progress) ? Math.max(0f, Math.min(1f, progress)) : 0f;
        float normalizedRate = visible && normalized < 1f && Float.isFinite(rate) ? Math.max(0f, rate) : 0f;
        boolean show = visible && (normalized > 0f || normalizedRate > 0f);
        long tick = player.serverLevel().getGameTime();
        var action = normalizedRate > 0 ? ChargeBarUpdateS2CPacket.Action.START : ChargeBarUpdateS2CPacket.Action.END;
        var ability = player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElse(null);
        if (ability != null) {
            State previous = ability.getChargeBarState(id);
            if (!show && previous == null) return true;
            if (show && previous != null) {
                float expected = Math.min(1f, previous.progress() + previous.rate() * (tick - previous.tick()));
                boolean changedRate = Float.compare(previous.rate(), normalizedRate) != 0;
                boolean discontinuity = Math.abs(expected - normalized) > 0.00001f;
                if (!changedRate && !discontinuity
                        && (normalizedRate == 0 || tick - previous.tick() < CORRECTION_INTERVAL)) return true;
                // A consumed/reset bar starts a new visual interval immediately.
                action = normalizedRate == 0 ? ChargeBarUpdateS2CPacket.Action.END
                        : previous.rate() == 0 || normalized < previous.progress()
                        ? ChargeBarUpdateS2CPacket.Action.START : ChargeBarUpdateS2CPacket.Action.CORRECT;
            }
            ability.setChargeBarState(id, show ? new State(normalized, normalizedRate, tick) : null);
        }
        ModMessages.sentToPlayer(new ChargeBarUpdateS2CPacket(id, action, show, normalized, normalizedRate), player);
        return true;
    }

    /** Clears actual charge and every currently synced indicator without knowing its source. */
    public static void clearAll(ServerPlayer player) {
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(ability -> {
            for (ResourceLocation id : ability.getVisibleChargeBarIds()) {
                syncToPlayer(id, false, 0f, player);
            }
            ability.clearChargeStates();
        });
    }
}
