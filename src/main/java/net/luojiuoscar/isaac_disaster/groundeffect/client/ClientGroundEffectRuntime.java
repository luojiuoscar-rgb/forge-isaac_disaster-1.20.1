package net.luojiuoscar.isaac_disaster.groundeffect.client;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectDespawnBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSyncS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client lifecycle for the lightweight ground-effect stream. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class ClientGroundEffectRuntime {
    public static final ClientGroundEffectRuntime INSTANCE = new ClientGroundEffectRuntime();
    private final GroundEffectStream stream = new GroundEffectStream();

    private ClientGroundEffectRuntime() {
    }

    public GroundEffectStream stream() { return stream; }

    public void applySync(GroundEffectSyncS2CPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().location().equals(packet.dimension())) return;
        if (packet.reset()) {
            if (!stream.beginSession(packet.epoch())) return;
        } else if (!stream.acceptEpoch(packet.epoch())) return;
        stream.synchronize(packet.serverTick());
    }

    public void applySnapshot(GroundEffectSnapshotBatchS2CPacket packet) {
        if (!stream.acceptEpoch(packet.epoch())) return;
        stream.synchronize(packet.serverTick());
        packet.entries().forEach(entry -> {
            if (packet.update()) stream.applyUpdate(entry, packet.serverTick());
            else stream.spawn(entry, packet.serverTick());
        });
    }

    public void applyDespawn(GroundEffectDespawnBatchS2CPacket packet) {
        if (!stream.acceptEpoch(packet.epoch())) return;
        stream.synchronize(packet.serverTick());
        if (packet.visibilityExit()) {
            packet.identities().forEach(identity -> stream.visibilityExit(
                    (int) (long) identity, (int) (identity >>> 32)));
        } else {
            packet.identities().forEach(stream::despawn);
        }
    }

    public void tick() { stream.tick(); }
    public void clear() { stream.clear(); }
    public void clearForWorldUnload() { stream.clearForWorldUnload(); }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().level == null) INSTANCE.clearForWorldUnload();
        else INSTANCE.tick();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        INSTANCE.clear();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) INSTANCE.clearForWorldUnload();
    }
}
