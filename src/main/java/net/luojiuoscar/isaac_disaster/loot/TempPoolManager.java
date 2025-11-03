package net.luojiuoscar.isaac_disaster.loot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootPool;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TempPoolManager {
    private static final Map<UUID, LootPool> tempPools = new ConcurrentHashMap<>();

    public static void put(ServerPlayer player, LootPool pool) {
        tempPools.put(player.getUUID(), pool);
    }

    public static LootPool get(ServerPlayer player) {
        return tempPools.remove(player.getUUID());
    }
}