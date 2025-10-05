package net.luojiuoscar.isaac_disaster.helper;

import com.google.common.util.concurrent.AtomicDouble;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.items.HolyMantle;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
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

    public static void removeItemFromId(int itemId, ServerPlayer player){
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> playerPassiveItem.removeFromId(player, itemId)
        );
    }

    public static void removeItemFromIndex(int itemId, ServerPlayer player){
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> playerPassiveItem.removeFromIndex(player, itemId)
        );
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

    /**
     * 对于*层数*相关的药水效果；移除1层
     */
    public static void removeAmplifier(Player player, MobEffect effect){
        MobEffectInstance effectI = player.getEffect(effect);
        if (effectI == null) return;

        int amplifier = effectI.getAmplifier() - 1;
        player.removeEffect(effect);

        if (amplifier < 0) return;


        MobEffectInstance newEffect = new MobEffectInstance(
                effect,
                -1,
                amplifier,
                true,
                false,
                true
        );
        player.addEffect(newEffect);
    }

    /**
     * 对于*层数*相关的药水效果；增加1层
     */
    public static void addAmplifier(Player player, MobEffect effect){
        MobEffectInstance effectI = player.getEffect(effect);
        int amplifier = 0;

        if (effectI != null) {
            amplifier = effectI.getAmplifier() + 1;
            player.removeEffect(effect);

            // 神圣护盾存在上限；最大层数10层
            if (effectI.getEffect() == ModEffects.HOLY_SHIELD.get()){
                amplifier = Math.min(9, amplifier);
            }
        }

        // 保险起见
        if (amplifier < 0) return;

        MobEffectInstance newEffect = new MobEffectInstance(
                effect,
                -1,
                amplifier,
                false,
                false,
                true
        );
        player.addEffect(newEffect);
    }

}

