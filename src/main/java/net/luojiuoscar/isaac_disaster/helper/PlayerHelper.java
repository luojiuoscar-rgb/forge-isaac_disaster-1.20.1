package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.*;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


public class PlayerHelper {

    public static void giveItem(Player player, ItemStack stack) {
        Level level = player.level();

        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        itemEntity.setNoPickUpDelay();
        level.addFreshEntity(itemEntity);
    }
    public static void giveItem(Player player, Item item, int count) {
        if (player.level().isClientSide()) return;

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
        if (player.level().isClientSide()) return;

        ItemStack stack = new ItemStack(item, count);
        Level level = player.level();

        ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        level.addFreshEntity(itemEntity);
    }
    public static boolean hasItem(int itemId, ServerPlayer player){
        int[] count = {0};
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .ifPresent(provider -> count[0] = provider.getItemCountFromAll(player, itemId));
        return count[0] > 0;
    }
    public static int getItemCount(int itemId, ServerPlayer player){
        int[] count = {0};
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .ifPresent(provider -> count[0] = provider.getItemCountFromAll(player, itemId));
        return count[0];
    }
    public static int getTrinketCount(int itemId, ServerPlayer player){
        int[] count = {0};
        player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS)
                .ifPresent(playerSwallowedTrinkets ->
                    count[0] = playerSwallowedTrinkets.getAllTrinketListFromId(player, itemId).size()
                );
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
    /**若amount为null则代表直接充满*/
    public static void chargeAll(ServerPlayer player, @Nullable Integer amount){
        Inventory inv = player.getInventory();
        List<ItemStack> invItems = new ArrayList<>();
        invItems.addAll(inv.items);
        invItems.addAll(inv.offhand);
        invItems.addAll(inv.armor);
        boolean canOverCharge = hasItem(ItemId.THE_BATTERY.getId(), player);
        // 遍历背包 对全部物品充能
        for (ItemStack stack : invItems) {
            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem &&
            !isFullCharge(stack, canOverCharge)) {
                if (amount == null) amount = stack.getMaxDamage() * 2; // 确保可以充满

                ActiveItem.modifyCharge(stack, amount, canOverCharge);
            }
        }
    }
    public static boolean isFullCharge(ItemStack stack, boolean canOverCharge){
        if (canOverCharge && !ActiveItem.getOverCharged(stack)) return false;
        return stack.getDamageValue() == 0;
    }
    public static void teleportPlayerToSpawn(ServerPlayer player) {
        // 获取玩家重生位置
        BlockPos respawnPos = player.getRespawnPosition();
        float respawnAngle = player.getRespawnAngle();
        ServerLevel spawnLevel = player.server.getLevel(player.getRespawnDimension());

        // 如果玩家没有设置床 / 自定义重生点，则使用主世界默认出生点
        if (respawnPos == null || spawnLevel == null) {
            spawnLevel = player.server.getLevel(Level.OVERWORLD);
            respawnPos = spawnLevel.getSharedSpawnPos();
            respawnAngle = player.getYRot(); // 随便保持原角度
        }

        // 将传送坐标转换为 Vec3 中心点
        Vec3 targetPos = Vec3.atCenterOf(respawnPos);

        // 若玩家在其他维度，需切换维度
        if (player.level() != spawnLevel) {
            player.teleportTo(spawnLevel, targetPos.x, targetPos.y, targetPos.z, respawnAngle, player.getXRot());
        } else {
            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            player.setYRot(respawnAngle);
        }
    }
    public static boolean getItemFlag(ServerPlayer player, int ItemId){
        boolean[] flag = {false};
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> flag[0] = playerAbility.getItemFlags().getOrDefault(ItemId, false)
        );
        return flag[0];
    }
    public static void setItemFlag(ServerPlayer player, int ItemId, boolean flag){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> playerAbility.setItemFlags(player, ItemId, flag)
        );
    }
    public static int countMoney(Player player) {
        int money = 0;

        // 从配置中读取三个钱币及其对应价值
        Item tier1Coin = getItemFromConfig(Config.COIN_TIER_1_ID.get());
        Item tier2Coin = getItemFromConfig(Config.COIN_TIER_2_ID.get());
        Item tier3Coin = getItemFromConfig(Config.COIN_TIER_3_ID.get());

        int value1 = Config.COIN_TIER_1_VALUE.get();
        int value2 = Config.COIN_TIER_2_VALUE.get();
        int value3 = Config.COIN_TIER_3_VALUE.get();

        Inventory inv = player.getInventory();
        List<ItemStack> items = new ArrayList<>();
        items.addAll(inv.items);
        items.addAll(inv.offhand);
        items.addAll(inv.armor);

        // 遍历背包所有物品
        for (ItemStack stack : items) {
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
    public static int countInvItem(Player player, Item item){
        int count = 0;

        Inventory inv = player.getInventory();

        List<ItemStack> items = new ArrayList<>();
        items.addAll(inv.items);
        items.addAll(inv.offhand);
        items.addAll(inv.armor);

        // 遍历背包所有物品
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() == item){
                count += stack.getCount();
            }
        }

        return count;
    }


    public static boolean hasSet(int itemId, ServerPlayer player){
        int[] count = {0};
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .ifPresent(provider -> count[0] = provider.getSetCountFromId(itemId));
        return count[0] > 0;
    }


    public static double getFly(Player player){
        AttributeInstance instance = player.getAttribute(ModAttributes.FLY_TIME.get());
        if (instance == null) return 0;
        return instance.getValue();
    }
    public static boolean canFly(Player player){
        AttributeInstance instance = player.getAttribute(ModAttributes.FLY_TIME.get());
        if (instance == null) return false;
        return instance.getValue() > 0;
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

        RandomSource random = player.getRandom();
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
    public static void shotBulletFromPlayer(Player player){
        int count = getBulletCount(player);

        // 发射子弹
        if (count <= 1){
            shotBullet(player);
        }else if(count == 2){
            shot2Bullet(player);
        }else {
            count = Math.min(count, 17); // 最大子弹数17
            float angleInterval = Math.max(11 - count, 5) * 2; // 子弹之间的间隔角度
            float curAngle = -angleInterval * (count - 1) / 2.0f;

            for (int i = 0; i < count; i++){
                shotBullet(player, player.getXRot(), player.getYRot() + curAngle);
                curAngle += angleInterval; // 修改角度
            }
        }
    }
    public static void shotBullet(Player player) {
        shotBullet(player, player.getXRot(), player.getYRot());
    }
    public static void shotBullet(Player player, float xRot, float yRot) {
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
                getBulletFilter(player),
                xRot,
                yRot
        );
        player.level().addFreshEntity(bullet);
    }
    public static void shot2Bullet(Player player){
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 eyePos = player.getEyePosition();

        IsaacBullet bullet1 = new IsaacBullet(
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
                getBulletFilter(player),
                player.getXRot(),
                player.getYRot(),
                eyePos.add(right.scale(0.25))
        );

        IsaacBullet bullet2 = new IsaacBullet(
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
                getBulletFilter(player),
                player.getXRot(),
                player.getYRot(),
                eyePos.add(right.scale(-0.25))
        );

        player.level().addFreshEntity(bullet1);
        player.level().addFreshEntity(bullet2);
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
        return Math.max(instance.getValue(),0.1);
    }
    public static double getBulletRange(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.BULLET_RANGE.get());
        if (instance == null) return 18.0;
        return  Math.min(Math.max(instance.getValue(), 4), 99);
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
    public static float getExtraBulletScale(Player player){
        // 额外子弹大小因子
        AttributeInstance extraBulletScale = player.getAttribute((ModAttributes.BULLET_SCALE.get()));
        float extraScale = 0.0f;
        if (extraBulletScale != null) extraScale = (float) extraBulletScale.getValue();
        return extraScale;
    }
    public static float getBulletScale(Player player){
        // 基于子弹伤害的体型因素
        AttributeInstance instance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        double damage = 1.0;
        if (instance == null) {
            return getBulletScale(1.0, getExtraBulletScale(player));
        };
        return getBulletScale(Math.max(instance.getValue(), 0), getExtraBulletScale(player));
    }
    public static float getBulletScale(double damage, float extraScale) {
        float scale = 1.0f;

        if (damage <= 198){
            scale *= (float) Math.log10(9 + damage / 2);
        }else {
            scale *= Math.min(4f, (float) (2 + Math.log10((damage-198) / 20))); // max 2.5f
        }

        return scale + extraScale;
    }
    public static int getBulletCount(Player player){
        RandomSource random = player.getRandom();

        AttributeInstance bullet_count = player.getAttribute(ModAttributes.BULLET_COUNT.get());
        if (bullet_count == null) return 1;
        // 获取子弹数量计数
        int[] count = {0};
        count[0] = (int) bullet_count.getValue();

        // theInnerEye & mutantSpider & perfectVision
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    int theInnerEye = playerPassiveItem.getItemCountFromAll(player, ItemId.THE_INNER_EYE.getId());
                    int mutantSpider = playerPassiveItem.getItemCountFromAll(player, ItemId.MUTANT_SPIDER.getId());
                    int perfectVision = playerPassiveItem.getItemCountFromAll(player, ItemId.PERFECT_VISION.getId());

                    if (perfectVision >= 1){
                        if (theInnerEye + mutantSpider == 0){
                            count[0] += 1; // 只有c -> 子弹+1
                        }else{
                            count[0] += perfectVision - 1; // 子弹+ c-1
                        }
                    }

                    if (theInnerEye + mutantSpider > 0){
                        count[0] += theInnerEye + 2*mutantSpider + 1;
                    }
                }
        );

        // 书虫套装（25%概率额外子弹）
        if (ClientDataManager.getInstance().getSetCountFromId(SetId.BOOK.getId()) >= 3 &&
                random.nextDouble() < 0.25){
            count[0] += 1;
        }


        return count[0];
    }


    // 衍生
    public static int getBulletLiftTime(Player player) {
        double speed = getBulletSpeed(player);
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
        // 当拥有双倍射击延迟时
        if (hasDoubleShotDelay(player)){
            delay *= 2;
        }

        return Math.max(delay, 0);
    }
    public static boolean hasDoubleShotDelay(Player player){
        int[] count = {0};
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER)
                .ifPresent(provider -> count[0] = provider.getDoubleShotDelay());

        // 当有完美视力时；取消双倍射击延迟
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    if (playerPassiveItem.getItemCountFromAll(player, ItemId.PERFECT_VISION.getId()) > 0){
                        count[0] = 0;
                    }
                });

        return count[0] > 0;
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
        AttributeInstance instance = player.getAttribute(ModAttributes.PILL_QUALITY.get());
        if (instance == null) return 0;
        return (int) instance.getValue();
    }

    public static void resetAllAttributes(ServerPlayer player){
        // 清除被动道具
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> playerPassiveItem.clearPlayerPassiveItems(player)
        );

        // 清除Attribute modifiers
        for (UUID uuid : StatManager.getAllUUID()){
            AttributeInstance instance = player.getAttribute(StatManager.fromUUID(uuid).getAttribute());
            if (instance != null){
                StatManager.removeModifier(player, instance, uuid); // 删除对应modifier
            }
        }

        // 重置cap
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(PlayerPassiveItem::init);
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(PlayerAbility::init);
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(PlayerStatModifier::init);

    }


    public static IsaacBomb spawnBombFromPlayer(ServerPlayer player, Vec3 tntVelocity){
        if (PlayerHelper.hasItem(ItemId.MR_MEGA.getId(), player)){
            return EntityHelper.spawnBomb(player.blockPosition().getCenter(), player, tntVelocity, 2);
        }else {
            return EntityHelper.spawnBomb(player.blockPosition().getCenter(), player, tntVelocity, 1);
        }
    }
    private static boolean isInsideSolidBlock(Level level, double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = level.getBlockState(pos);
        return !state.getCollisionShape(level, pos).isEmpty();
    }
    public static void spawnRandomBombsNearby(ServerPlayer player, double range, int count){
        final double RANGE = StatManager.getNearbyRange() * 0.5;
        final int MAX_ATTEMPTS = 20;
        RandomSource random = player.getRandom();

        for (int i = 0; i < 6; i++) {
            IsaacBomb tnt = PlayerHelper.spawnBombFromPlayer(player, Vec3.ZERO);
            if (tnt == null) continue;

            boolean placed = false;
            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                // 随机
                double offsetX = (random.nextDouble() * 2 * RANGE) - RANGE;
                double offsetY = (random.nextDouble() * RANGE); // 在玩家y轴的上方
                double offsetZ = (random.nextDouble() * 2 * RANGE) - RANGE;

                double x = player.getX() + offsetX;
                double y = player.getY() + offsetY;
                double z = player.getZ() + offsetZ;

                if (!isInsideSolidBlock(player.level(), x, y, z)) {
                    tnt.setPos(x, y, z);
                    placed = true;
                    break;
                }
            }

            // 默认放玩家脚底
            if (!placed) {
                tnt.setPos(player.getX(), player.getY(), player.getZ());
            }
        }
    }

    public static int swallowAllTrinkets(Player player){
        int[] count = {0};
        List<ItemStack> stackList = CuriosHelper.getEquippedItemsInSlot(player, CuriosHelper.TRINKET);
        player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                playerSwallowedTrinkets -> {
                    for (ItemStack stack : stackList){
                        playerSwallowedTrinkets.swallow(player, stack);
                        count[0]++;
                    }
                });
        return count[0];
    }

    public static int countPlayer(Predicate<ServerPlayer> filter){
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 0;
        return (int) server.getPlayerList().getPlayers().stream().filter(filter).count();
    }
}

