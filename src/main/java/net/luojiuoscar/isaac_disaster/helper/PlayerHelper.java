package net.luojiuoscar.isaac_disaster.helper;

import com.google.common.util.concurrent.AtomicDouble;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerHelper {

    public static void giveItem(Player player, Item item, int count) {
        if (player.level().isClientSide()) return; // 只在服务端生成掉落物

        ItemStack stack = new ItemStack(item, count);
        Level level = player.level();

        // 实际上是掉落n个无拾起冷却的物品
        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        itemEntity.setNoPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    public static void spawnItem(Player player, Item item, int count) {
        if (player.level().isClientSide()) return; // 只在服务端生成掉落物

        ItemStack stack = new ItemStack(item, count);
        Level level = player.level();

        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        level.addFreshEntity(itemEntity);
    }

    public static boolean hasItem(int itemId, ServerPlayer player){
        AtomicInteger count = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> count.set(playerPassiveItem.getItemCount(itemId))
        );
        return count.get() > 0;
    }

    public static int getItemCount(int itemId, ServerPlayer player){
        AtomicInteger count = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> count.set(playerPassiveItem.getItemCount(itemId))
        );
        return count.get();
    }

    public static boolean canFly(ServerPlayer player){
        AtomicDouble flyTime = new AtomicDouble();
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> flyTime.set(playerStatModifier.getFlyTime())
        );
        return flyTime.get() > 0;
    }

    public static boolean isFlyLimit(ServerPlayer player){
        return getFlyPercentage(player) >= 1;
    }

    public static double getFlyPercentage(ServerPlayer player){
        AtomicDouble flyTime = new AtomicDouble();
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> flyTime.set(playerStatModifier.getFlyTime())
        );

        AtomicDouble currentFlyTime = new AtomicDouble();
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> currentFlyTime.set(playerStatModifier.getFlyTimeCurrent())
        );

        return currentFlyTime.get() / flyTime.get();
    }

}

