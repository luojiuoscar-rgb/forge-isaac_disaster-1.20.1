package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemSyncS2CPacket;
import net.minecraft.server.level.ServerPlayer;

public interface INeedSyncToClient {
    ThreadLocal<Boolean> syncing = ThreadLocal.withInitial(() -> false);

    default void sync(ServerPlayer player, int itemId) {
        if (syncing.get()) return; // 防止重入
        syncing.set(true);
        try {
            int count = PlayerHelper.getItemCount(itemId, player);
            ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(itemId, count), player);
        } finally {
            syncing.set(false);
        }
    }

}
