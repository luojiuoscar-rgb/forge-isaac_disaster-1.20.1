package net.luojiuoscar.isaac_disaster.datapack;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.datapack.sync.GameplaySettingsS2CPacket;
import net.luojiuoscar.isaac_disaster.event.IsaacFlightEvents;
import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Validation is staged; a failed reload in any listener cannot publish partial settings. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public final class IsaacDatapackEvents {
    private static final Map<ResourceManager, IsaacDatapackTypes.Snapshot> PREPARED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private IsaacDatapackEvents() {}

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<IsaacDatapackTypes.Snapshot>() {
            @Override protected IsaacDatapackTypes.Snapshot prepare(ResourceManager resources, ProfilerFiller profiler) {
                return loadForSync(resources);
            }

            @Override protected void apply(IsaacDatapackTypes.Snapshot data, ResourceManager resources, ProfilerFiller profiler) {
                PREPARED.put(resources, data);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void beforeServerStart(ServerAboutToStartEvent event) {
        var server = event.getServer();
        IsaacDatapackData.install(server, preparedOrLoad(server.getResourceManager()));
        IsaacDisaster.LOGGER.info("Loaded Isaac Disaster gameplay settings from world datapacks");
    }

    private static IsaacDatapackTypes.Snapshot preparedOrLoad(ResourceManager resources) {
        var prepared = PREPARED.get(resources);
        // Fresh worlds may reuse preview listeners with a different resource manager.
        return prepared == null ? loadForSync(resources) : prepared;
    }

    private static IsaacDatapackTypes.Snapshot loadForSync(ResourceManager resources) {
        var data = IsaacDatapackTypes.load(resources);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new GameplaySettingsS2CPacket(data).toBytes(buffer);
        } finally {
            buffer.release();
        }
        return data;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void sync(OnDatapackSyncEvent event) {
        var server = event.getPlayerList().getServer();
        if (event.getPlayer() == null) {
            // Forge fires this after the entire reload succeeded and installed the resource manager.
            IsaacDatapackData.install(server, preparedOrLoad(server.getResourceManager()));
            for (var player : server.getPlayerList().getPlayers()) {
                DefaultAttributeManager.apply(player);
                CurioSlotManager.requestReconcile(player);
                StatManager.refreshMultipliers(player);
                IsaacFlightEvents.sendState(player);
            }
        }
        var packet = new GameplaySettingsS2CPacket(IsaacDatapackData.forServer(server));
        event.getPlayers().forEach(player -> ModMessages.sentToPlayer(packet, player));
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        IsaacDatapackData.clearServer(event.getServer());
        PREPARED.clear();
    }
}
