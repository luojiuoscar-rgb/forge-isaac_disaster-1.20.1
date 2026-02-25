package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemPools;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemPoolsProvider;
import net.luojiuoscar.isaac_disaster.manager.data.ServerItemPoolsData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Set;

public class PoolHelper {

    private PoolHelper() {}

    public static void markAsRemoval(Player player, ResourceLocation rl, int itemId) {
        // 根据配置决定要走全池移除还是单池移除（由 Config 控制）
        if (Config.ITEM_REMOVAL_FROM_ALL_POOL.get()) {
            removeFromAll(player, itemId);
        } else if (Config.ITEM_REMOVAL_FROM_POOL.get()) {
            removeFromPool(player, rl, itemId);
        }
    }

    public static void removeFromPool(Player player, ResourceLocation rl, int itemId) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            ServerItemPoolsData.get(serverLevel).removeFromPool(rl, itemId);
        } else {
            player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .ifPresent(cap -> cap.removeFromPool(rl, itemId));
        }
    }

    public static void removeFromAll(Player player, int itemId) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            ServerItemPoolsData.get(serverLevel).removeFromAll(itemId);
        } else {
            player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .ifPresent(cap -> cap.removeFromAll(itemId));
        }
    }

    public static void addToPool(Player player, ResourceLocation rl, int itemId) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            ServerItemPoolsData.get(serverLevel).addToPool(rl, itemId);
        } else {
            player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .ifPresent(cap -> cap.addToPool(rl, itemId));
        }
    }

    public static void addToAll(Player player, int itemId) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            ServerItemPoolsData.get(serverLevel).addToAll(itemId);
        } else {
            player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .ifPresent(cap -> cap.addToAll(itemId));
        }
    }

    // -----------------------
    // 读取 / 查询方法
    // -----------------------

    public static boolean isRemoved(Player player, ResourceLocation rl, int itemId) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return false;

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            return ServerItemPoolsData.get(serverLevel).isRemoved(rl, itemId);
        } else {
            return player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .map(cap -> cap.isRemoved(rl, itemId))
                    .orElse(false);
        }
    }

    public static boolean isAdded(Player player, ResourceLocation rl, int itemId) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return false;

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            return ServerItemPoolsData.get(serverLevel).isAdded(rl, itemId);
        } else {
            return player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .map(cap -> cap.isAdded(rl, itemId))
                    .orElse(false);
        }
    }


    public static Set<Integer> getRemoval(Player player, ResourceLocation rl) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return Collections.emptySet();

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            return ServerItemPoolsData.get(serverLevel).getRemoval(rl);
        } else {
            return player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .map(cap -> cap.getRemoval(rl))
                    .orElse(Collections.emptySet());
        }
    }


    public static Set<Integer> getAddition(Player player, ResourceLocation rl) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return Collections.emptySet();

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            return ServerItemPoolsData.get(serverLevel).getAddition(rl);
        } else {
            return player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .map(cap -> cap.getAddition(rl))
                    .orElse(Collections.emptySet());
        }
    }

    // -----------------------
    // 清空 / 重置
    // -----------------------
    public static void clear(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        if (Config.PLAYERS_SHARE_ITEM_POOLS.get()) {
            ServerItemPoolsData.get(serverLevel).clear();
        } else {
            player.getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL)
                    .ifPresent(PlayerItemPools::init);
        }
    }
}
