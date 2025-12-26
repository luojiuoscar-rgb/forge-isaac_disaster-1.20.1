package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.capability.player.*;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GetShotDelayEvent;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.TrinketId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
        return player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .map(p -> p.getItemCountFromAll(player, itemId) > 0)
                .orElse(false);
    }
    public static int getItemCount(int itemId, ServerPlayer player){
        return player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .map(p -> p.getItemCountFromAll(player, itemId))
                .orElse(0);
    }
    public static boolean hasTrinket(int itemId, ServerPlayer player){
        return hasTrinket(itemId, player, false);
    }
    public static boolean hasTrinket(int itemId, ServerPlayer player, boolean onlyEnchanted){
        return player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS)
                .map(playerSwallowedTrinkets -> {
                    List<ItemStack> stackList = playerSwallowedTrinkets.getAllTrinketListFromId(player, itemId);
                    if (onlyEnchanted) {
                        return stackList.stream().anyMatch(ItemStack::isEnchanted);
                    } else {
                        return !stackList.isEmpty();
                    }
                })
                .orElse(false);
    }
    public static int getTrinketCount(int itemId, ServerPlayer player){
        return player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS)
                .map(p -> p.getAllTrinketListFromId(player, itemId).size())
                .orElse(0);

    }
    public static double getValueFromTrinket(double normal, double enchanted, int itemId, ServerPlayer player){
        if (!hasTrinket(itemId, player)) return 0;
        return hasTrinket(itemId, player, true) ? normal : enchanted;
    }


    public static void removeItemFromId(ResourceLocation itemId, ServerPlayer player){
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> playerPassiveItem.removeFromId(player, itemId)
        );
    }
    public static void removeItemFromIndex(int index, ServerPlayer player){
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> playerPassiveItem.removeFromIndex(player, index)
        );
    }
    /**若amount为null则代表直接充满*/
    public static void chargeAll(ServerPlayer player, @Nullable Integer amount){
        boolean canOverCharge = hasItem(ItemId.THE_BATTERY.getId(), player);
        chargeAll(player, amount, canOverCharge);
    }
    public static void chargeAll(ServerPlayer player, @Nullable Integer amount, boolean canOverCharge){
        Inventory inv = player.getInventory();
        List<ItemStack> invItems = new ArrayList<>();
        invItems.addAll(inv.items);
        invItems.addAll(inv.offhand);

        // 遍历背包 对全部物品充能
        for (ItemStack stack : invItems) {
            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem &&
            !isFullCharge(stack, canOverCharge)) {
                if (amount == null) amount = stack.getMaxDamage() * 2; // 确保可以充满

                chargeItem(stack, amount, canOverCharge);
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
    public static boolean takeMoney(Player player, int amount) {
        // 读取配置
        int value1 = Config.COIN_TIER_1_VALUE.get();
        int value2 = Config.COIN_TIER_2_VALUE.get();
        int value3 = Config.COIN_TIER_3_VALUE.get();

        Item tier1Coin = getItemFromConfig(Config.COIN_TIER_1_ID.get());
        Item tier2Coin = getItemFromConfig(Config.COIN_TIER_2_ID.get());
        Item tier3Coin = getItemFromConfig(Config.COIN_TIER_3_ID.get());

        int total = countMoney(player);
        if (total < amount) {
            return false;
        }

        Inventory inv = player.getInventory();
        List<ItemStack> all = new ArrayList<>();
        all.addAll(inv.items);
        all.addAll(inv.offhand);

        for (ItemStack stack : all) {
            Item item = stack.getItem();
            if (item == tier1Coin || item == tier2Coin || item == tier3Coin) {
                stack.shrink(stack.getCount());
            }
        }

        int remain = total - amount;

        int tier3Count = remain / value3;
        remain %= value3;

        int tier2Count = remain / value2;
        remain %= value2;

        int tier1Count = remain / value1;

        giveItem(player, tier3Coin, tier3Count);
        giveItem(player, tier2Coin, tier2Count);
        giveItem(player, tier1Coin, tier1Count);

        return true;
    }

    public static int countInvItem(Player player, Item item){
        int count = 0;

        Inventory inv = player.getInventory();

        List<ItemStack> items = new ArrayList<>();
        items.addAll(inv.items);
        items.addAll(inv.offhand);


        // 遍历背包所有物品
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() == item){
                count += stack.getCount();
            }
        }

        return count;
    }


    public static boolean hasSet(ResourceLocation id, ServerPlayer player){
        return player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM)
                .map(p -> p.getSetCountFromId(id) > 3)
                .orElse(false);
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
    public static void teleportToRandomLocation(Entity entity, double radius) {
        if (entity.level().isClientSide()) return;

        Level world = entity.level();
        Vec3 currentPos = entity.position();
        int minY = world.getMinBuildHeight();
        int maxY = world.getMaxBuildHeight();

        RandomSource random = world.getRandom();
        float playerHeight = entity.getBbHeight();

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
                entity.teleportTo(
                        safePos.getX() + 0.5, // 方块中心X
                        safePos.getY() + 0.1, // 稍微高于方块
                        safePos.getZ() + 0.5  // 方块中心Z
                );
                return;
            }
        }
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

        return true;
    }


    // 基础
    public static double getTears(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.TEARS.get());
        if (instance == null) return 0.0;

        return  Math.max(instance.getValue(),-7);
    }
    public static double getTearsCorrection(Player player) {
        AttributeInstance instance = player.getAttribute(ModAttributes.TEARS_CORRECTION.get());
        MobEffectInstance effect = player.getEffect(MobEffects.DIG_SPEED);

        double value = 0;
        value += effect != null ? effect.getAmplifier() + 1 : 0;
        value += instance != null ? instance.getValue() : 0;

        return  value;
    }
    public static float getExtraBulletScale(Player player){
        // 额外子弹大小因子
        AttributeInstance extraBulletScale = player.getAttribute((ModAttributes.BULLET_SCALE.get()));
        float extraScale = 0.0f;
        if (extraBulletScale != null) extraScale = (float) extraBulletScale.getValue();
        return extraScale;
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


    // 衍生
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

        GetShotDelayEvent event = new GetShotDelayEvent(player, delay);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()){
            delay = event.getDelay();
        }

        return Math.max(delay, 0);
    }

    public static double getFireRate(Player player) {
        return 20 / getShotDelay(player);
    }

    // 属性
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
            return EntityHelper.spawnBomb(player.blockPosition().getCenter(), player, player.level(), tntVelocity, 2);
        }else {
            return EntityHelper.spawnBomb(player.blockPosition().getCenter(), player, player.level(), tntVelocity, 1);
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
                        playerSwallowedTrinkets.swallow(stack);
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

    public static void unlockBlock(Player player, InteractionHand hand, BlockPos pos, int paperClipRequirements, Runnable onUnlock){
        Level level = player.level();
        ItemStack held = player.getItemInHand(hand);


        int paperClip = PlayerHelper.getTrinketCount(TrinketId.PAPER_CLIP.getId(), (ServerPlayer) player);

        if (held.is(ModItems.KEY.get()) || held.is(ModItems.GOLDEN_KEY.get()) || (paperClip >= paperClipRequirements)){ // 钥匙 or 金钥匙 or paperClip
            onUnlock.run(); // behaviour

            if (held.is(ModItems.KEY.get()) && !player.isCreative()){
                player.getItemInHand(hand).shrink(1); // 钥匙-1
            }
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    ModSounds.UNLOCK.get(), SoundSource.BLOCKS, 0.7f, 1.0f);
        }else{
            player.displayClientMessage(
                    Component.translatable("block.isaac_disaster.message.locked"), true);
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_LOCKED,
                    SoundSource.BLOCKS, 1.0f ,1.0f);
        }
    }


    public static void chargeItem(ItemStack stack, int amount, boolean canOverCharge){
        boolean OverCharged = ActiveItem.getOverCharged(stack);
        // 检查物品是否耐久不满 或可以过载
        if ((stack.getDamageValue() > 0)
                || (canOverCharge || !OverCharged)) {
            // 计算新的耐久值
            int newDamage = Math.max(stack.getDamageValue() - amount, 0);

            // 未过载且有蓄电池时过载
            if (newDamage == 0 && !OverCharged && canOverCharge){
                newDamage = stack.getMaxDamage() - 1;
                ActiveItem.setOverCharged(stack, true);
            }

            stack.setDamageValue(newDamage);
        }
    }

    public static boolean teleportToNearestIdentifier(ServerPlayer player, ResourceLocation identifier) {
        ServerLevel level = (ServerLevel) player.level();

        Set<BlockPos> posSet = BlockData.get(level).getIdentifiers(identifier);

        if (posSet.isEmpty()) return false;

        BlockPos playerBlockPos = player.blockPosition();
        Optional<BlockPos> nearest = posSet.stream()
                .min(Comparator.comparingDouble(pos -> pos.distSqr(playerBlockPos)));

        if (nearest.isEmpty()) return false;

        nearest.ifPresent(pos -> {
            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;

            player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
        });
        return true;
    }


    public static void copyNearestPedestal(ServerPlayer player, boolean linked) {
        ServerLevel level = (ServerLevel) player.level();
        BlockData data = BlockData.get(level);
        Set<BlockPos> pedestals = data.getAllPedestals();
        if (pedestals.isEmpty()) return;

        BlockPos playerPos = player.blockPosition();
        pedestals.stream()
                .min(Comparator.comparingDouble(pos -> pos.distSqr(playerPos)))
                .ifPresent(nearest -> copyPedestalAt(level, nearest, linked));
    }

    private static void copyPedestalAt(ServerLevel level, BlockPos sourcePos, boolean linked) {
        BlockEntity sourceBE = level.getBlockEntity(sourcePos);
        if (!(sourceBE instanceof PedestalBlockEntity originalPedestal)) return;

        for (int dx : new int[]{-1, 0, 1}) {
            for (int dz : new int[]{-1, 0, 1}) {
                BlockPos newPos = sourcePos.offset(dx, 0, dz);
                if (!level.getBlockState(newPos).getCollisionShape(level, newPos).isEmpty()) continue;

                level.setBlock(newPos, ModBlocks.PEDESTAL_BLOCK.get().defaultBlockState(), 3);

                BlockEntity newBE = level.getBlockEntity(newPos);
                if (newBE instanceof PedestalBlockEntity newPedestal) {
                    newPedestal.copyFromOriginal(originalPedestal);
                    if (linked) PedestalBlockEntity.linkPedestals(sourcePos, newPos, level);
                    return; // 只复制到第一个空格
                }
            }
        }
    }

    public static boolean isSelfDamage(DamageSource source){
        return source.is(DamageTypes.GENERIC_KILL);
    }

    public static boolean isHitAllowedType(DamageSource source){
        return source.is(DamageTypes.PLAYER_ATTACK)
                || source.is(DamageTypes.GENERIC)
                || source.is(DamageTypes.MOB_PROJECTILE)
                || source.is(DamageTypes.ARROW)
                || source.is(DamageTypes.TRIDENT)
                || source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO);
    }
}

