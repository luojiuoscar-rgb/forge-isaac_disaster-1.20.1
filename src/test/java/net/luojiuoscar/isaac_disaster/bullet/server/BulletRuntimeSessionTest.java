package net.luojiuoscar.isaac_disaster.bullet.server;

import net.luojiuoscar.isaac_disaster.bullet.client.BulletStream;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BulletRuntimeSessionTest {
    @Test
    void overworldNetherOverworldRoundTripStillAcceptsAndRetainsNewBullets() throws Exception {
        BulletRuntime runtime = runtime();
        ServerLevel overworld = allocate(ServerLevel.class);
        ServerLevel nether = allocate(ServerLevel.class);
        ServerPlayer player = player(UUID.randomUUID());
        BulletStream client = new BulletStream();
        int first = epoch(runtime, overworld, player);
        assertTrue(client.acceptEpoch(first));
        client.spawn(0, 1, 0, BulletState.builder().lifetime(20).build());
        runtime.onPlayerChangedDimension(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, Level.NETHER));
        client.clearForWorldUnload();
        int second = epoch(runtime, nether, player);
        assertTrue(client.acceptEpoch(second));
        runtime.onPlayerChangedDimension(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.NETHER, Level.OVERWORLD));
        client.clearForWorldUnload();
        int returned = epoch(runtime, overworld, player);
        assertTrue(returned > second, "Returning to an existing level requires a newer stream epoch");
        assertTrue(client.acceptEpoch(returned));
        BulletState fresh = client.spawn(0, 1, 0, BulletState.builder().lifetime(20).build());
        assertFalse(client.acceptEpoch(second), "An old Nether packet must remain rejected");
        assertSame(fresh, client.get(0, 1));
    }

    @Test
    void replacingADeadPlayerRefreshesTheSessionEvenInTheSameWorld() throws Exception {
        BulletRuntime runtime = runtime();
        ServerLevel level = allocate(ServerLevel.class);
        UUID id = UUID.randomUUID();
        ServerPlayer dead = player(id);
        ServerPlayer respawned = player(id);
        int old = epoch(runtime, level, dead);
        assertEquals(old, epoch(runtime, level, dead));
        assertTrue(epoch(runtime, level, respawned) > old,
                "A new ServerPlayer with the same UUID cannot reuse a dead player's stream");
    }

    @Test
    void changingOnePlayersSessionDoesNotResetOtherPlayersInTheWorld() throws Exception {
        BulletRuntime runtime = runtime();
        ServerLevel level = allocate(ServerLevel.class);
        ServerLevel nether = allocate(ServerLevel.class);
        ServerPlayer first = player(UUID.randomUUID());
        ServerPlayer second = player(UUID.randomUUID());
        int firstEpoch = epoch(runtime, level, first);
        int secondEpoch = epoch(runtime, level, second);
        runtime.onPlayerChangedDimension(new PlayerEvent.PlayerChangedDimensionEvent(first, Level.OVERWORLD, Level.NETHER));
        int next = epoch(runtime, nether, first);
        assertTrue(next > firstEpoch);
        assertEquals(secondEpoch, epoch(runtime, level, second));
    }

    @Test
    void respawnEventClearsVisibilityAndRefreshesTheCurrentSession() throws Exception {
        BulletRuntime runtime = runtime();
        ServerLevel level = allocate(ServerLevel.class);
        ServerPlayer player = player(UUID.randomUUID());
        int old = epoch(runtime, level, player);
        seedVisibility(runtime, "visible", level, player.getUUID());
        seedVisibility(runtime, "hidden", level, player.getUUID());
        runtime.onPlayerRespawn(new PlayerEvent.PlayerRespawnEvent(player, false));
        assertTrue(epoch(runtime, level, player) > old);
        assertFalse(hasVisibility(runtime, "visible", level, player.getUUID()));
        assertFalse(hasVisibility(runtime, "hidden", level, player.getUUID()));
    }

    private static BulletRuntime runtime() throws Exception {
        var constructor = BulletRuntime.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static int epoch(BulletRuntime runtime, ServerLevel level, ServerPlayer player) throws Exception {
        Method method = BulletRuntime.class.getDeclaredMethod("epoch", ServerLevel.class, ServerPlayer.class);
        method.setAccessible(true);
        return (int) method.invoke(runtime, level, player);
    }

    @SuppressWarnings("unchecked")
    private static Map<ServerLevel, Map<UUID, Set<Long>>> visibility(BulletRuntime runtime, String name) throws Exception {
        Field field = BulletRuntime.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<ServerLevel, Map<UUID, Set<Long>>>) field.get(runtime);
    }

    private static void seedVisibility(BulletRuntime runtime, String name, ServerLevel level, UUID player) throws Exception {
        visibility(runtime, name).computeIfAbsent(level, ignored -> new HashMap<>()).put(player, Set.of(1L));
    }

    private static boolean hasVisibility(BulletRuntime runtime, String name, ServerLevel level, UUID player) throws Exception {
        return visibility(runtime, name).getOrDefault(level, Map.of()).containsKey(player);
    }

    private static ServerPlayer player(UUID id) throws Exception {
        ServerPlayer player = allocate(ServerPlayer.class);
        player.setUUID(id);
        return player;
    }

    private static <T> T allocate(Class<T> type) throws Exception {
        Class<?> unsafe = Class.forName("sun.misc.Unsafe");
        Field field = unsafe.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(unsafe.getMethod("allocateInstance", Class.class).invoke(field.get(null), type));
    }
}
