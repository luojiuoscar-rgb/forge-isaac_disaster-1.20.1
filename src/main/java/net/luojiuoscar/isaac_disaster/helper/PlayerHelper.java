package net.luojiuoscar.isaac_disaster.helper;

import com.google.common.util.concurrent.AtomicDouble;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.projectile.IsaacBullet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

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

    public static int countMoney(Player player){
        int money = 0;

        // 从配置中读取三个钱币及其对应价值
        Item tier1Coin = getItemFromConfig(Config.COIN_TIER_1_ID.get());
        Item tier2Coin = getItemFromConfig(Config.COIN_TIER_2_ID.get());
        Item tier3Coin = getItemFromConfig(Config.COIN_TIER_3_ID.get());

        int value1 = Config.COIN_TIER_1_VALUE.get();
        int value2 = Config.COIN_TIER_2_VALUE.get();
        int value3 = Config.COIN_TIER_3_VALUE.get();

        // 遍历背包所有物品
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            if (item == tier1Coin) {
                money += stack.getCount() * value1;
            } else if (item == tier2Coin) {
                money += stack.getCount() * value2;
            } else if (item == tier3Coin) {
                money += stack.getCount() * value3;
            }
        }

        return money;
    }

    /**
     * 根据 config 的字符串创建物品引用
     * 例如 "minecraft:gold_nugget" → 对应物品
     */
    public static Item getItemFromConfig(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) return null;
        return ForgeRegistries.ITEMS.getValue(id);
    }



    // 子弹相关
    public static void shotBullet(Player player){
        IsaacBullet bullet = new IsaacBullet(
                player.level(),
                player,
                PlayerHelper.getBulletLiftTime(player),
                PlayerHelper.getBulletSpeed(player),
                PlayerHelper.getBulletScale(player)
        );
        player.level().addFreshEntity(bullet);
    }
    // 基础
    public static double getBulletSpeed(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_SPEED.get());
        if (instance == null) return 1.0;
        return 0.8*Math.max(instance.getValue(),0.1);
    }
    public static double getBulletRange(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_RANGE.get());
        if (instance == null) return 18.0;
        return  Math.min(Math.max(instance.getValue(),1), 99);
    }
    public static double getTears(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.TEARS.get());
        if (instance == null) return 0.0;

        return  Math.max(instance.getValue(),-7);
    }
    public static double getTearsCorrection(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.TEARS_CORRECTION.get());
        if (instance == null) return 0.0;

        return  instance.getValue();
    }
    public static float getBulletScale(Player player) {
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        float scale = 1.0f;
        if (instance == null) return scale;
        double damage = Math.max(instance.getValue(), 0);

        return (float) (scale * (Math.log(1 + damage)));
    }

    // 衍生
    public static int getBulletLiftTime(Player player) {
        double speed =  getBulletSpeed(player);
        double range = getBulletRange(player);
        // 计算所需 ticks
        return (int) Math.min(Math.max(1, range / speed), 200);
    }
    public static double getShotDelay(Player player) {
        double tears = getTears(player);
        double delay;

        if (tears < -(10.0/13.0)){
            delay = (11 - 4*tears);
        }else if(tears >= -(10.0/13.0) && tears < 0){
            delay = (11 - 4*Math.sqrt(1.3*tears+1) - 4*tears);
        }else if(tears >= 0 && tears < (165.0/104.0)){
            delay = (11 - 4*Math.sqrt(1.3*tears+1));
        }else{
            delay =  4;
        }
        delay -= getTearsCorrection(player);

        return Math.max(delay, 0);
    }
    public static double getFireRate(Player player) {
        return 20 / getShotDelay(player);
    }



}

