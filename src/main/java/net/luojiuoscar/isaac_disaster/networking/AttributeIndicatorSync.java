package net.luojiuoscar.isaac_disaster.networking;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.networking.packet.AttributeIndicatorSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.system.attribute_indicator.AttributeSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AttributeIndicatorSync {
    private record SentSnapshot(int entityId, AttributeSnapshot snapshot) {
    }

    private static final Map<UUID, SentSnapshot> LAST_SENT = new HashMap<>();

    private AttributeIndicatorSync() {
    }

    public static void tick(ServerPlayer player) {
        SentSnapshot previous = LAST_SENT.get(player.getUUID());
        boolean baseline = previous == null || previous.entityId() != player.getId();
        if (!baseline && player.tickCount % 5 != 0) return;

        AttributeSnapshot snapshot = new AttributeSnapshot(
                value(player, Attributes.MOVEMENT_SPEED),
                20.0 / Math.max(1.0, PlayerHelper.getShotDelay(player)),
                value(player, Attributes.ATTACK_DAMAGE),
                value(player, ModAttributes.BULLET_RANGE.get()),
                value(player, ModAttributes.BULLET_SPEED.get()),
                value(player, Attributes.LUCK));
        if (!snapshot.isFinite()) return;
        if (baseline || !snapshot.equals(previous.snapshot())) {
            ModMessages.sentToPlayer(new AttributeIndicatorSyncS2CPacket(snapshot, baseline), player);
            LAST_SENT.put(player.getUUID(), new SentSnapshot(player.getId(), snapshot));
        }
    }

    private static double value(ServerPlayer player, Attribute attribute) {
        var instance = player.getAttribute(attribute);
        return instance == null ? 0 : instance.getValue();
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        LAST_SENT.remove(event.getOriginal().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LAST_SENT.clear();
    }
}
