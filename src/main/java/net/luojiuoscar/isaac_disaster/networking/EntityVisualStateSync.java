package net.luojiuoscar.isaac_disaster.networking;

import net.luojiuoscar.isaac_disaster.networking.packet.EntityVisualStateS2CPacket;
import net.luojiuoscar.isaac_disaster.system.EntityVisualState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class EntityVisualStateSync {
    private EntityVisualStateSync() {
    }

    public static void syncToTracking(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }

        ModMessages.sendToTracking(snapshot(entity), entity);
    }

    public static void syncToPlayer(LivingEntity entity, ServerPlayer player) {
        ModMessages.sentToPlayer(snapshot(entity), player);
    }

    private static EntityVisualStateS2CPacket snapshot(LivingEntity entity) {
        return new EntityVisualStateS2CPacket(
                entity.getId(),
                entity.getUUID(),
                EntityVisualState.getFreezeSources(entity),
                EntityVisualState.getActiveVisualLayers(entity)
        );
    }
}
