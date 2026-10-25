package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.holylight.HolyLightSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Owns per-world simulation and per-player visibility; no EntityType or global ground-effect manager. */
public final class HolyLightRuntime {
    public static final HolyLightRuntime INSTANCE = new HolyLightRuntime();
    public static final double VISIBLE_RADIUS = 48;
    private final Map<ServerLevel, HolyLightManager> managers = new WeakHashMap<>();
    private final Map<UUID, Session> sessions = new HashMap<>();
    private int nextEpoch = 1;

    private HolyLightRuntime() { }

    public HolyLightManager manager(ServerLevel level) {
        return managers.computeIfAbsent(level, ignored -> new HolyLightManager());
    }

    @SubscribeEvent
    public void onTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        var manager = managers.get(level);
        if (manager == null) return;
        manager.tick(level);
        synchronize(level, manager.snapshots());
    }

    private void synchronize(ServerLevel level, List<HolyLightSnapshot> snapshots) {
        long now = level.getGameTime();
        for (var player : level.players()) {
            var dimension = level.dimension().location();
            var previous = sessions.get(player.getUUID());
            if (previous == null || !previous.dimension.equals(dimension)) {
                previous = new Session(nextEpoch++, dimension);
                sessions.put(player.getUUID(), previous);
                ModMessages.sentToPlayer(new HolyLightSyncS2CPacket(previous.epoch, dimension, now,
                        true, List.of(), List.of()), player);
                previous.lastClock = now;
            }
            Set<Long> current = new HashSet<>();
            List<HolyLightSnapshot> added = new ArrayList<>();
            for (var snapshot : snapshots) {
                if (snapshot.position().distanceToSqr(player.position()) > VISIBLE_RADIUS * VISIBLE_RADIUS) continue;
                current.add(snapshot.id());
                if (!previous.visible.contains(snapshot.id())) added.add(snapshot);
            }
            List<Long> removed = previous.visible.stream().filter(id -> !current.contains(id)).toList();
            if (!added.isEmpty() || !removed.isEmpty() || (!current.isEmpty() && now - previous.lastClock >= 20)) {
                ModMessages.sentToPlayer(new HolyLightSyncS2CPacket(previous.epoch, dimension, now,
                        false, added, removed), player);
                previous.lastClock = now;
            }
            previous.visible = current;
        }
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var manager = managers.remove(level);
        if (manager != null) manager.clear();
        sessions.values().removeIf(session -> session.dimension.equals(level.dimension().location()));
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { sessions.remove(event.getEntity().getUUID()); }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) { sessions.remove(event.getEntity().getUUID()); }

    @SubscribeEvent
    public void onStop(ServerStoppingEvent event) {
        managers.values().forEach(HolyLightManager::clear);
        managers.clear();
        sessions.clear();
    }

    private static final class Session {
        private final int epoch;
        private final ResourceLocation dimension;
        private Set<Long> visible = Set.of();
        private long lastClock;

        private Session(int epoch, ResourceLocation dimension) {
            this.epoch = epoch;
            this.dimension = dimension;
        }
    }
}
