package net.luojiuoscar.isaac_disaster.groundeffect.server;

import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectPlacement;
import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.WaterPuddleEffect;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectDespawnBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSyncS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/** Lifecycle and visibility bridge for non-entity ground effects. */
public final class GroundEffectRuntime {
    public static final GroundEffectRuntime INSTANCE = new GroundEffectRuntime();
    public static final double VISIBLE_RADIUS = 48.0D;
    private static final int CLOCK_INTERVAL = 20;

    private final Map<ServerLevel, GroundEffectManager> managers = new WeakHashMap<>();
    private final GroundEffectSessions sessions = new GroundEffectSessions();

    private GroundEffectRuntime() {
    }

    public GroundEffectManager manager(ServerLevel level) {
        return managers.computeIfAbsent(level, ignored -> new GroundEffectManager());
    }

    public int activeCount(ServerLevel level) {
        return manager(level).activeCount();
    }

    public int activeCount(MinecraftServer server) {
        if (server == null) return 0;
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            GroundEffectManager manager = managers.get(level);
            if (manager != null) count += manager.activeCount();
        }
        return count;
    }

    @Nullable
    public WaterPuddleEffect spawnWater(ServerPlayer owner) {
        if (owner == null || owner.level().isClientSide()) return null;
        return GroundEffectPlacement.projectToGround(owner.serverLevel(),
                        new Vec3(owner.getX(), owner.getY(), owner.getZ()))
                .map(position -> manager(owner.serverLevel()).spawn(ModGroundEffects.WATER.get(), owner.getUUID(),
                        position, WaterPuddleEffect.DEFAULT_RADIUS, WaterPuddleEffect.DEFAULT_LIFETIME))
                .orElse(null);
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;
        if (event.level instanceof ServerLevel level) {
            GroundEffectManager manager = managers.get(level);
            if (manager == null) return;
            manager.tick(level);
            synchronizeVisible(level, manager);
        }
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) clearLevel(level);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        managers.clear();
        sessions.clear();
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clearPlayer(player.getUUID());
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clearPlayer(player.getUUID());
    }

    private void synchronizeVisible(ServerLevel level, GroundEffectManager manager) {
        Set<Long> eventDespawns = manager.drainDespawns();
        int serverTick = (int) level.getGameTime();
        for (ServerPlayer player : level.players()) {
            var session = sessions.get(player.getUUID(), level.dimension().location());
            if (!session.initialized) {
                ModMessages.sentToPlayer(new GroundEffectSyncS2CPacket(session.epoch(),
                        level.dimension().location(), serverTick, true), player);
                session.initialized = true;
                session.lastClockTick = serverTick;
            }
            Map<Long, Integer> previous = session.visible();
            Map<Long, Integer> current = new HashMap<>();
            List<GroundEffectSnapshotBatchS2CPacket.Entry> toSpawn = new ArrayList<>();
            List<GroundEffectSnapshotBatchS2CPacket.Entry> toUpdate = new ArrayList<>();
            for (AbstractGroundEffect effect : manager.queryVisible(player.position(), VISIBLE_RADIUS)) {
                current.put(effect.identity(), effect.revision());
                Integer lastRevision = previous.get(effect.identity());
                if (lastRevision == null) {
                    toSpawn.add(GroundEffectSnapshotBatchS2CPacket.Entry.from(effect));
                } else if (lastRevision != effect.revision()) {
                    toUpdate.add(GroundEffectSnapshotBatchS2CPacket.Entry.from(effect));
                }
            }
            if (!toSpawn.isEmpty()) {
                ModMessages.sentToPlayer(new GroundEffectSnapshotBatchS2CPacket(session.epoch(), serverTick, false, toSpawn), player);
            }
            if (!toUpdate.isEmpty()) {
                ModMessages.sentToPlayer(new GroundEffectSnapshotBatchS2CPacket(session.epoch(), serverTick, true, toUpdate), player);
            }

            Set<Long> exits = new HashSet<>(previous.keySet());
            exits.removeAll(current.keySet());
            exits.removeAll(eventDespawns);
            Set<Long> terminal = new HashSet<>();
            for (Long identity : eventDespawns) if (previous.containsKey(identity)) terminal.add(identity);
            if (!exits.isEmpty()) {
                ModMessages.sentToPlayer(new GroundEffectDespawnBatchS2CPacket(session.epoch(),
                        serverTick, true, new ArrayList<>(exits)), player);
            }
            if (!terminal.isEmpty()) {
                ModMessages.sentToPlayer(new GroundEffectDespawnBatchS2CPacket(session.epoch(),
                        serverTick, false, new ArrayList<>(terminal)), player);
            }
            if (!current.isEmpty() && serverTick - session.lastClockTick >= CLOCK_INTERVAL) {
                ModMessages.sentToPlayer(new GroundEffectSyncS2CPacket(session.epoch(),
                        level.dimension().location(), serverTick, false), player);
                session.lastClockTick = serverTick;
            }
            previous.clear();
            previous.putAll(current);
        }
    }

    private void clearPlayer(UUID playerId) {
        sessions.remove(playerId);
    }

    private void clearLevel(ServerLevel level) {
        GroundEffectManager manager = managers.remove(level);
        if (manager != null) manager.clear();
        sessions.removeDimension(level.dimension().location());
    }
}
