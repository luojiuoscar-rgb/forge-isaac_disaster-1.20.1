package net.luojiuoscar.isaac_disaster.bullet.server;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletCorrectionBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletCorrectionS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletDespawnBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletTrackingBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletShatterS2CPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/** Server-side owner of transient bullet managers, one manager per level. */
public final class BulletRuntime {
    public static final BulletRuntime INSTANCE = new BulletRuntime();
    /** Maximum distance at which a player receives transient bullet stream data. */
    public static final double VISIBLE_RADIUS = 128.0D;
    private static final long TRACKING_HANDLE_GRACE_TICKS = 80L;
    private final Map<ServerLevel, BulletManager> managers = new WeakHashMap<>();
    private final Map<ServerLevel, Map<UUID, Set<Long>>> visible = new WeakHashMap<>();
    private final Map<ServerLevel, Integer> streamEpochs = new WeakHashMap<>();
    private final Map<ServerLevel, TrackingHandleState> trackingHandles = new WeakHashMap<>();
    private int nextEpoch = 1;
    private BulletRuntime() {}

    /** Returns the transient manager associated with one server level. */
    public BulletManager manager(ServerLevel level) {
        return managers.computeIfAbsent(level, ignored -> new BulletManager(epoch(level)));
    }

    /** Returns the number of active optimized bullets across all currently loaded dimensions. */
    public int activeCount(MinecraftServer server) {
        if (server == null) return 0;
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            BulletManager manager = managers.get(level);
            if (manager != null) count += manager.activeCount();
        }
        return count;
    }

    /** Removes optimized bullets from all loaded dimensions and notifies every affected client stream. */
    public int clearAll(MinecraftServer server) {
        if (server == null) return 0;
        int removed = 0;
        for (ServerLevel level : server.getAllLevels()) {
            BulletManager manager = managers.get(level);
            if (manager == null) continue;
            removed += manager.clear().size();
            trackingHandles.remove(level);
            Map<UUID, Set<Long>> levelVisible = visible.remove(level);
            if (levelVisible == null) continue;
            for (ServerPlayer player : level.players()) {
                Set<Long> playerVisible = levelVisible.get(player.getUUID());
                if (playerVisible != null && !playerVisible.isEmpty()) {
                    ModMessages.sentToPlayer(new BulletDespawnBatchS2CPacket(epoch(level), new ArrayList<>(playerVisible)), player);
                }
            }
        }
        return removed;
    }

    /** Creates a trajectory-free state from an attack context on the server. */
    public @Nullable BulletState spawn(@Nullable AttackContext context) {
        if (context == null || context.getOwner() == null) {
            IsaacDisaster.LOGGER.warn("Discarded optimized bullet with no attack owner or context");
            return null;
        }
        ServerLevel level = context.getOwner().level() instanceof ServerLevel server ? server : null;
        if (level == null) {
            IsaacDisaster.LOGGER.warn("Discarded optimized bullet created outside a server level");
            return null;
        }
        double speed = Math.max(0.1D, context.getBulletSpeed());
        int lifetime = (int) Math.min(Math.max(1.0D, context.getBulletRange() / speed), 200.0D);
        return spawn(level, BulletState.from(context).lifetime(lifetime).build());
    }

    /** Registers a prepared state and publishes its immutable spawn snapshot to the owner. */
    public @Nullable BulletState spawn(ServerLevel level, @Nullable BulletState state) {
        if (level == null || state == null) {
            IsaacDisaster.LOGGER.warn("Discarded optimized bullet with invalid level or state");
            return null;
        }
        BulletState spawned = manager(level).spawn(state);
        return spawned;
    }

    /** Ticks level managers at the end of each server level tick. */
    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;
        if (event.level instanceof ServerLevel level) {
            BulletManager manager = managers.get(level);
            if (manager != null) manager.tick(level);
            if (manager != null) synchronizeVisible(level, manager);
        }
    }

    /** Clears level-owned transient references at the same lifecycle boundary as other server systems. */
    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        clearLevel(level);
    }

    /** Clears all transient managers when a server stops without waiting for weak references. */
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        managers.clear();
        visible.clear();
        streamEpochs.clear();
        trackingHandles.clear();
    }

    /** Drops server-side visibility state when a player disconnects so reconnects receive snapshots. */
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        clearPlayerVisibility(player.getUUID());
    }

    /** Removes stale stream state before a player can later re-enter a previously visited dimension. */
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clearPlayerVisibility(player.getUUID());
    }

    /** Clears one player's visibility cache from all level streams. */
    private void clearPlayerVisibility(UUID playerId) {
        for (Map<UUID, Set<Long>> levelVisible : visible.values()) levelVisible.remove(playerId);
        visible.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /** Drops one level's manager, pending packets, visibility table, and handle table. */
    private void clearLevel(ServerLevel level) {
        BulletManager manager = managers.remove(level);
        if (manager != null) manager.clear();
        visible.remove(level);
        streamEpochs.remove(level);
        trackingHandles.remove(level);
    }

    /** Sends only newly visible stream entries and removes entries that left the visibility radius. */
    private void synchronizeVisible(ServerLevel level, BulletManager manager) {
        Map<UUID, Set<Long>> levelVisible = visible.computeIfAbsent(level, ignored -> new HashMap<>());
        List<BulletState> active = manager.activeStates();
        // Consume authoritative removals before rebuilding each player's visible set.
        // The set-difference below handles range exits; this list also covers bullets
        // removed during the tick before they can appear in the active snapshot.
        List<Long> eventDespawns = manager.drainDespawns();
        for (ServerPlayer player : level.players()) {
            Set<Long> previous = levelVisible.computeIfAbsent(player.getUUID(), ignored -> new HashSet<>());
            Set<Long> current = new HashSet<>();
            List<BulletSpawnS2CPacket> toSpawn = new ArrayList<>();
            for (BulletState state : active) {
                if (player.distanceToSqr(state.position().x, state.position().y, state.position().z) > VISIBLE_RADIUS * VISIBLE_RADIUS) continue;
                long key = identity(state.slot(), state.generation());
                current.add(key);
                // The state may have steered or moved between creation and this end-of-tick broadcast.
                // A current snapshot keeps newly visible clients in the same kinematic state as the server.
                if (!previous.contains(key)) toSpawn.add(snapshot(level, state));
            }
            if (!toSpawn.isEmpty()) ModMessages.sentToPlayer(new BulletSpawnBatchS2CPacket(toSpawn), player);
            Set<Long> despawn = new HashSet<>(previous);
            despawn.removeAll(current);
            for (Long identity : eventDespawns) {
                if (previous.contains(identity)) despawn.add(identity);
            }
            if (!despawn.isEmpty()) ModMessages.sentToPlayer(new BulletDespawnBatchS2CPacket(epoch(level), new ArrayList<>(despawn)), player);
            previous.clear();
            previous.addAll(current);
        }
        List<BulletCorrectionS2CPacket> corrections = manager.drainCorrections();
        if (!corrections.isEmpty() || (level.getGameTime() & 3L) == 0L) {
            for (ServerPlayer player : level.players()) {
            List<BulletCorrectionS2CPacket> visibleCorrections = new ArrayList<>();
            for (BulletCorrectionS2CPacket correction : corrections) {
                    BulletState state = manager.get(correction.slot(), correction.generation());
                    if (state != null && player.distanceToSqr(correction.position().x, correction.position().y, correction.position().z)
                    <= VISIBLE_RADIUS * VISIBLE_RADIUS) visibleCorrections.add(correction);
            }
            if (!visibleCorrections.isEmpty()) ModMessages.sentToPlayer(new BulletCorrectionBatchS2CPacket(epoch(level), visibleCorrections), player);
            }
        }
        List<BulletShatterS2CPacket.Entry> shatters = manager.drainShatters();
        if (!shatters.isEmpty()) {
            for (ServerPlayer player : level.players()) {
                List<BulletShatterS2CPacket.Entry> visibleShatters = shatters.stream()
                        .filter(entry -> player.distanceToSqr(entry.position().x, entry.position().y, entry.position().z)
                                <= VISIBLE_RADIUS * VISIBLE_RADIUS).toList();
                if (!visibleShatters.isEmpty()) ModMessages.sentToPlayer(
                        new BulletShatterS2CPacket(epoch(level), visibleShatters), player);
            }
        }
        synchronizeTracking(level, active);
    }

    /** Publishes one four-tick steering snapshot using anonymous stream-local target handles. */
    private void synchronizeTracking(ServerLevel level, List<BulletState> active) {
        if ((level.getGameTime() & 3L) != 0L) return;
        TrackingHandleState handles = trackingHandles.computeIfAbsent(level, ignored -> new TrackingHandleState());
        handles.cleanup(level.getGameTime());
        for (ServerPlayer player : level.players()) {
            List<BulletTrackingBatchS2CPacket.Assignment> assignments = new ArrayList<>();
            List<BulletTrackingBatchS2CPacket.VelocitySample> velocitySamples = new ArrayList<>();
            Map<Integer, Vec3> positions = new HashMap<>();
            for (BulletState state : active) {
                if (!(state.isHoming() || state.isControllable()) || player.distanceToSqr(state.position().x, state.position().y, state.position().z)
                        > VISIBLE_RADIUS * VISIBLE_RADIUS) continue;
                int handle = handles.handleFor(state, level.getGameTime());
                state.setTrackingHandle(handle);
                assignments.add(new BulletTrackingBatchS2CPacket.Assignment(state.slot(), state.generation(), handle,
                        state.trackingUsesControl()));
                velocitySamples.add(new BulletTrackingBatchS2CPacket.VelocitySample(state.slot(), state.generation(),
                        state.velocity(), state.acceleration()));
                if (handle != 0 && state.trackingPosition() != null) positions.put(handle, state.trackingPosition());
            }
            if (assignments.isEmpty()) continue;
            List<BulletTrackingBatchS2CPacket.TargetSample> targets = new ArrayList<>(positions.size());
            for (Map.Entry<Integer, Vec3> entry : positions.entrySet()) {
                targets.add(new BulletTrackingBatchS2CPacket.TargetSample(entry.getKey(), entry.getValue()));
            }
            ModMessages.sentToPlayer(new BulletTrackingBatchS2CPacket(epoch(level), (int) level.getGameTime(), targets, assignments, velocitySamples), player);
        }
    }

    private static BulletSpawnS2CPacket snapshot(ServerLevel level, BulletState state) {
        return new BulletSpawnS2CPacket(INSTANCE.epoch(level), state.slot(), state.generation(), (int) level.getGameTime(), state.position(),
                state.previousPosition(), state.velocity(), state.acceleration(), state.baseSpeed(), state.age(), state.lifetime(), state.traveled(),
                state.getRange(), state.getDamage(), state.renderScale(),
                state.collisionWidth(), state.collisionHeight(), state.color(), state.alpha(), new ArrayList<>(state.visualIds()),
                state.getSourceType() == net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType.FETUS_BULLET,
                state.ownerUuid(), state.isHoming(), state.isControllable(), state.homingRange(), state.homingSteer(),
                state.controlRange(), state.controlSteer());
    }

    private static long identity(int slot, int generation) {
        return ((long) generation << 32) | (slot & 0xFFFFFFFFL);
    }

    /** Allocates a server-session stream epoch lazily for each loaded level. */
    private int epoch(ServerLevel level) {
        return streamEpochs.computeIfAbsent(level, ignored -> nextEpoch++);
    }

    /** Assigns stable anonymous handles to real targets and owner-look control points. */
    private static final class TrackingHandleState {
        private final Map<UUID, Integer> entityHandles = new HashMap<>();
        private final Map<UUID, Integer> controlHandles = new HashMap<>();
        private final Map<UUID, Long> entityLastSeen = new HashMap<>();
        private final Map<UUID, Long> controlLastSeen = new HashMap<>();
        private int nextHandle = 1;

        /** Returns a stable handle and refreshes its retention timestamp. */
        private int handleFor(BulletState state, long gameTime) {
            if (state.trackingPosition() == null) return 0;
            UUID key = state.trackingUsesControl()
                    ? state.ownerUuid()
                    : state.trackingTarget() == null ? null : state.trackingTarget().getUUID();
            if (key == null) return 0;
            Map<UUID, Integer> handles = state.trackingUsesControl() ? controlHandles : entityHandles;
            Map<UUID, Long> lastSeen = state.trackingUsesControl() ? controlLastSeen : entityLastSeen;
            lastSeen.put(key, gameTime);
            return handles.computeIfAbsent(key, ignored -> nextHandle++);
        }

        /** Removes handles that have not been referenced for the network grace window. */
        private void cleanup(long gameTime) {
            cleanupMap(entityHandles, entityLastSeen, gameTime);
            cleanupMap(controlHandles, controlLastSeen, gameTime);
        }

        private static void cleanupMap(Map<UUID, Integer> handles, Map<UUID, Long> lastSeen, long gameTime) {
            lastSeen.entrySet().removeIf(entry -> gameTime - entry.getValue() > TRACKING_HANDLE_GRACE_TICKS);
            handles.keySet().removeIf(key -> !lastSeen.containsKey(key));
        }
    }
}
