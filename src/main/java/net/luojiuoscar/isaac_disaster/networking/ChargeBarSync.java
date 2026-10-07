package net.luojiuoscar.isaac_disaster.networking;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
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
        ModMessages.sentToPlayer(new ChargeBarUpdateS2CPacket(id, visible, progress), player);
        return true;
    }
}
