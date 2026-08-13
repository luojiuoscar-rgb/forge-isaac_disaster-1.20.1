package net.luojiuoscar.isaac_disaster.system;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.accessor.LivingEntityFreezeAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Calculates the server-authoritative time-stop flag and maintains player position snapshots. */
public final class TimeStopState {
    private static final Map<ResourceKey<Level>, Set<UUID>> SERVER_SOURCES = new HashMap<>();
    private static final Map<PlayerSnapshotKey, PositionSnapshot> SERVER_PLAYER_POSITIONS = new HashMap<>();
    private static final Map<PlayerSnapshotKey, PositionSnapshot> CLIENT_PLAYER_POSITIONS = new HashMap<>();
    private static Level clientLevel;

    private TimeStopState() {
    }

    public static void refreshServer(MinecraftServer server) {
        // Only the players can create time-stop sources, so this avoids scanning loaded entities.
        SERVER_SOURCES.clear();

        for (ServerLevel level : server.getAllLevels()) {
            Set<UUID> sources = new HashSet<>();
            for (ServerPlayer player : level.players()) {
                if (player.hasEffect(ModEffects.THE_WORLD.get())) {
                    sources.add(player.getUUID());
                }
            }
            if (!sources.isEmpty()) {
                SERVER_SOURCES.put(level.dimension(), sources);
            }
        }
    }

    public static void updateMobFreezeState(Mob mob) {
        // This runs before serverAiStep, including on mobs whose AI was frozen on the previous tick.
        if (mob.level().isClientSide()) {
            return;
        }
        setFrozen(mob, shouldFreeze(mob, mob.level().dimension()));
    }

    public static void updatePlayerFreezeState(ServerPlayer player) {
        // Players keep ticking; only their position and velocity are restored while frozen.
        if (player.level().isClientSide()) {
            return;
        }

        boolean frozen = shouldFreeze(player, player.level().dimension());
        setFrozen(player, frozen);

        PlayerSnapshotKey key = new PlayerSnapshotKey(player.getUUID(), player.level().dimension());
        if (!frozen) {
            SERVER_PLAYER_POSITIONS.remove(key);
            return;
        }

        PositionSnapshot snapshot = SERVER_PLAYER_POSITIONS.computeIfAbsent(key,
                ignored -> new PositionSnapshot(player.getX(), player.getY(), player.getZ()));
        restorePosition(player, snapshot);
    }

    public static boolean isTimeStopTarget(LivingEntity entity) {
        // Clients read the synchronized result and never calculate sources or team relations.
        if (entity instanceof LivingEntityFreezeAccess access) {
            return access.isaacDisaster$isFrozen();
        }
        return false;
    }

    public static void tickClientPlayers(Level level) {
        // The client only enforces the server-provided position lock for players.
        if (level == null || !level.isClientSide()) {
            return;
        }

        if (clientLevel != level) {
            clearClientPlayerSnapshots();
            clientLevel = level;
        }

        Set<PlayerSnapshotKey> activeSnapshots = new HashSet<>();
        for (Player player : level.players()) {
            if (!isTimeStopTarget(player)) {
                continue;
            }

            PlayerSnapshotKey key = new PlayerSnapshotKey(player.getUUID(), level.dimension());
            activeSnapshots.add(key);
            PositionSnapshot snapshot = CLIENT_PLAYER_POSITIONS.computeIfAbsent(key,
                    ignored -> new PositionSnapshot(player.getX(), player.getY(), player.getZ()));
            restorePosition(player, snapshot);
        }

        CLIENT_PLAYER_POSITIONS.keySet().removeIf(key -> !activeSnapshots.contains(key));
    }

    public static void clearPlayerSnapshots(UUID playerUuid) {
        SERVER_PLAYER_POSITIONS.keySet().removeIf(key -> key.uuid().equals(playerUuid));
        CLIENT_PLAYER_POSITIONS.keySet().removeIf(key -> key.uuid().equals(playerUuid));
        SERVER_SOURCES.values().forEach(sourceIds -> sourceIds.remove(playerUuid));
        SERVER_SOURCES.values().removeIf(Set::isEmpty);
    }

    public static void clearPlayerSnapshots(Level level) {
        if (level == null) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        SERVER_PLAYER_POSITIONS.keySet().removeIf(key -> key.dimension().equals(dimension));
        CLIENT_PLAYER_POSITIONS.keySet().removeIf(key -> key.dimension().equals(dimension));
        SERVER_SOURCES.remove(dimension);
    }

    public static void clearAllPlayerSnapshots() {
        SERVER_SOURCES.clear();
        SERVER_PLAYER_POSITIONS.clear();
        clearClientPlayerSnapshots();
    }

    public static void clearClientPlayerSnapshots() {
        CLIENT_PLAYER_POSITIONS.clear();
        clientLevel = null;
    }

    private static boolean shouldFreeze(LivingEntity entity, ResourceKey<Level> dimension) {
        // No source in this dimension means any stale synchronized freeze state must clear.
        Set<UUID> sourceIds = SERVER_SOURCES.get(dimension);
        if (sourceIds == null || sourceIds.isEmpty() || entity.isDeadOrDying()) {
            return false;
        }

        // The source players are explicitly immune to their own time stop.
        if (entity instanceof ServerPlayer player) {
            if (player.hasEffect(ModEffects.THE_WORLD.get())) {
                return false;
            }
        // Time stop targets ordinary mobs and other players, but never bosses or non-mobs.
        } else if (!(entity instanceof Mob) || entity.getType().is(TagManager.BOSSES)) {
            return false;
        }

        // When enabled, one friendly source is enough to exempt the target.
        if (Config.TIME_STOP_EXCLUDE_FRIENDLY.get()) {
            for (UUID sourceId : sourceIds) {
                Player source = entity.level().getPlayerByUUID(sourceId);
                if (source instanceof ServerPlayer serverSource
                        && EntityHelper.isFriendly(entity, serverSource)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void setFrozen(LivingEntity entity, boolean frozen) {
        if (entity instanceof LivingEntityFreezeAccess access
                && access.isaacDisaster$isFrozen() != frozen) {
            access.isaacDisaster$setFrozen(frozen);
        }
    }

    private static void restorePosition(Player player, PositionSnapshot snapshot) {
        player.setPos(snapshot.x(), snapshot.y(), snapshot.z());
        player.setDeltaMovement(Vec3.ZERO);
    }

    private record PlayerSnapshotKey(UUID uuid, ResourceKey<Level> dimension) {
    }

    private record PositionSnapshot(double x, double y, double z) {
    }
}
