package net.luojiuoscar.isaac_disaster.helper;


import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;


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
    public static void giveMoney(Player player, int amount){
        int value = Config.COIN_TIER_1_VALUE.get();
        String moneyId = Config.COIN_TIER_1_ID.get();

        if (value <= 0) value = 1;
        // 计算需要给予的数量
        int count = (int) Math.ceil((double) amount / value);

        ResourceLocation rl = ResourceLocation.tryParse(moneyId);
        if (rl == null) return;
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        // 检查是否有效
        if (item == null || item == Items.AIR) return;
        giveItem(player, item, count);
    }
    public static void spawnItem(Player player, Item item, int count) {
        if (player.level().isClientSide()) return; // 只在服务端生成掉落物

        ItemStack stack = new ItemStack(item, count);
        Level level = player.level();

        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        level.addFreshEntity(itemEntity);
    }
    public static boolean hasItem(int itemId, ServerPlayer player){
        int[] count = {0};
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .ifPresent(provider -> count[0] = provider.getItemCount(itemId));
        return count[0] > 0;
    }
    public static int getItemCount(int itemId, ServerPlayer player){
        int[] count = {0};
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .ifPresent(provider -> count[0] = provider.getItemCount(itemId));
        return count[0];
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


    public static boolean hasSet(int itemId, ServerPlayer player){
        int[] count = {0};
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .ifPresent(provider -> count[0] = provider.getSetCountFromId(itemId));
        return count[0] > 0;
    }


    public static boolean canFly(ServerPlayer player){
        double[] count = {0};
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER)
                .ifPresent(provider -> count[0] = provider.getFlyTime());
        return count[0] > 0;
    }
    public static boolean isFlyLimit(ServerPlayer player){
        return getFlyPercentage(player) >= 1;
    }
    public static double getFlyPercentage(ServerPlayer player){
        double[] count = {0, 0};
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER)
                .ifPresent(provider -> count[0] = provider.getFlyTime());
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER)
                .ifPresent(provider -> count[1] = provider.getFlyTimeCurrent());

        return count[1] / count[0];
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

    /**
     * 将玩家传送到一个随机安全位置
     */
    public static void teleportToRandomLocation(Player player, double radius) {
        // 只在服务端执行传送逻辑
        if (player.level().isClientSide()) {
            return;
        }

        Level world = player.level();
        Vec3 currentPos = player.position();
        int minY = world.getMinBuildHeight();
        int maxY = world.getMaxBuildHeight();

        Random random = new Random();
        float playerHeight = player.getBbHeight();

        // 最多尝试5次
        for (int retry = 0; retry < 5; retry++) {
            // 生成随机X和Z坐标（在半径范围内）
            int randomX = (int) (currentPos.x + (random.nextDouble() * radius * 2 - radius));
            int randomZ = (int) (currentPos.z + (random.nextDouble() * radius * 2 - radius));
            // 生成初始Y坐标（在世界高度范围内）
            int initialY = (int) (currentPos.y + (random.nextDouble() * radius * 2 - radius));
            initialY = Math.max(minY, Math.min(maxY, initialY));

            // 从初始Y位置向上寻找安全位置
            BlockPos safePos = findSafePositionUpwards(world, randomX, randomZ, initialY, maxY, playerHeight);

            if (safePos != null) {
                // 找到安全位置，执行传送（Y坐标稍微向上偏移，避免卡进方块）
                player.teleportTo(
                        safePos.getX() + 0.5, // 方块中心X
                        safePos.getY() + 0.1, // 稍微高于方块
                        safePos.getZ() + 0.5  // 方块中心Z
                );
                return;
            }
        }

        // 达到最大尝试次数仍未找到合适位置
    }
    private static BlockPos findSafePositionUpwards(Level world, int x, int z, int startY, int maxY, double playerHeight) {
        BlockPos currentPos = new BlockPos(x, startY, z);

        // 从初始Y向上搜索，直到达到世界高度上限
        for (int y = startY; y <= maxY; y++) {
            currentPos = currentPos.atY(y);

            // 检查当前位置是否可以安全站立
            if (isSafeToStand(world, currentPos, playerHeight)) {
                return currentPos;
            }
        }

        // 到达世界上限仍未找到安全位置
        return null;
    }
    private static boolean isSafeToStand(Level world, BlockPos pos, double playerHeight) {
        // 检查脚下是否有支撑（有碰撞箱）
        BlockPos standPos = pos.below();
        BlockState groundState = world.getBlockState(standPos);
        if (groundState.getCollisionShape(world, standPos).isEmpty()) {
            return false; // 脚下无支撑，不安全
        }

        // 计算需要检查的总高度（向上取整，确保覆盖完整碰撞箱）
        int checkHeight = (int) Math.ceil(playerHeight);

        // 检查玩家身体占据的每一格空间是否有碰撞箱
        for (int y = 0; y < checkHeight; y++) {
            BlockPos bodyPos = pos.offset(0, y, 0); // 当前检查的身体位置
            BlockState bodyState = world.getBlockState(bodyPos);

            // 如果身体位置存在碰撞箱，则不安全
            if (!bodyState.getCollisionShape(world, bodyPos).isEmpty()) {
                return false;
            }
        }

        // 所有检查通过，位置安全
        return true;
    }



    // 子弹相关
    public static void shotBullet(Player player){
        IsaacBullet bullet = new IsaacBullet(
                player.level(),
                player,
                getBulletLiftTime(player),
                getBulletSpeed(player),
                getBulletScale(player),
                isSpectral(player),
                isPiercing(player),
                isHoming(player),
                isControllable(player),
                getDamage(player),
                getBulletColor(player),
                getBulletAlpha(player),
                getBulletFilter(player)
        );

        player.level().addFreshEntity(bullet);
    }
    public static boolean isSpectral(Player player){
        int[] count = {0};
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                PlayerAbility -> count[0] = (PlayerAbility.getSpectral())
        );
        return count[0] > 0;
    }
    public static boolean isHoming(Player player){
        int[] count = {0};
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                PlayerAbility -> count[0] = (PlayerAbility.getHoming())
        );
        return count[0] > 0;
    }
    public static boolean isPiercing(Player player){
        int[] count = {0};
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                PlayerAbility -> count[0] = (PlayerAbility.getPiercing())
        );
        return count[0] > 0;
    }
    public static boolean isControllable(Player player){
        int[] count = {0};
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                PlayerAbility -> count[0] = (PlayerAbility.getControllable())
        );
        return count[0] > 0;
    }
    public static int getBulletColor(Player player){
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_COLOR.get());
        if (instance != null) return (int) instance.getValue();
        return ColorManager.COLOR_BASE;
    }
    public static float getBulletAlpha(Player player){
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_ALPHA.get());
        if (instance != null) return (float) instance.getValue();
        return 1.0f;
    }
    public static int getBulletFilter(Player player){
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_FILTER.get());
        if (instance != null) return (int) instance.getValue();
        return ColorManager.FILTER_BASE;
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

        if (damage <= 198){
            scale *= (float) Math.log(1 + damage / 2); // max 2
        }else if (damage > 198 && damage < 398){
            scale *= (float) (2 + Math.log((damage-198) / 20)); // max 3
        }else{
            scale = 3.0f;
        }
        // 额外子弹大小因子
        AttributeInstance extraBulletScale = player.getAttribute((ModAttributes.BULLET_SCALE.get()));
        float extraScale = 0.0f;
        if (extraBulletScale != null) extraScale = (float) extraBulletScale.getValue();

        return scale + extraScale;
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

    // 属性
    public static float getDamage(Player player){
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance != null) return (float) instance.getValue();
        return 0.0f;
    }
    public static int getPillQuality(Player player){
        int[] count = {0};
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                PlayerAbility -> count[0] = (PlayerAbility.getPillQuality())
        );
        return count[0];
    }
    public static void modifyPillQuality(Player player, int amount){
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer){
            serverPlayer.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                    playerAbility -> playerAbility.setPillQuality(serverPlayer ,playerAbility.getPillQuality() + amount)
            );
        }
    }

}

