package net.luojiuoscar.isaac_disaster.networking;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Validates charge updates before constructing a packet for the shared network sender. */
public final class ChargeBarSync {
    private ChargeBarSync() {
    }

    public static boolean syncToPlayer(ResourceLocation id, boolean visible, float progress, ServerPlayer player) {
        if (id == null || player == null) {
            IsaacDisaster.LOGGER.warn("Skipping charge bar sync with missing registry ID or recipient: {}", id);
            return false;
        }
        float normalized = Float.isFinite(progress) ? Math.max(0f, Math.min(1f, progress)) : 0f;
        boolean show = visible && normalized > 0f;
        var ability = player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElse(null);
        if (ability != null) {
            Float previous = ability.getChargeBarProgress(id);
            if (show && previous != null && Float.compare(previous, normalized) == 0) return true;
            if (!show && previous == null) return true;
            ability.setChargeBarProgress(id, show ? normalized : null);
        }
        ModMessages.sentToPlayer(new ChargeBarUpdateS2CPacket(id, show, normalized), player);
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
