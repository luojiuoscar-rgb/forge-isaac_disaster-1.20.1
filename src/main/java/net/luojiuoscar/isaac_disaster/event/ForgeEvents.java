package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffectProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.*;
import net.luojiuoscar.isaac_disaster.commands.item.ItemClearCmd;
import net.luojiuoscar.isaac_disaster.commands.item.ItemGetCmd;
import net.luojiuoscar.isaac_disaster.commands.item.ItemSpawnCmd;
import net.luojiuoscar.isaac_disaster.commands.pill.PillShuffleCmd;
import net.luojiuoscar.isaac_disaster.commands.pill.PillTriggerByEffectCmd;
import net.luojiuoscar.isaac_disaster.commands.player.PlayerRefreshPoolCmd;
import net.luojiuoscar.isaac_disaster.commands.player.PlayerResetDataCmd;
import net.luojiuoscar.isaac_disaster.commands.trinket.TrinketClearSwallowedCmd;
import net.luojiuoscar.isaac_disaster.commands.trinket.TrinketSetEnchanted;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.IIsaacCuriosItem;
import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.item.item.custom.FoodPassiveItem;
import net.luojiuoscar.isaac_disaster.item.pickup.special.IsaacHead;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemMapSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.PillRecordsSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.RefreshScaleS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.SetCountSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.server.command.ConfigCommand;
import top.theillusivec4.curios.api.event.CurioUnequipEvent;

import java.util.List;
import java.util.Map;

import static net.luojiuoscar.isaac_disaster.IsaacDisaster.MOD_ID;


@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {
    // 世界加载事件
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (level.dimension() != Level.OVERWORLD) return;
            PillEffectManager.getInstance().loadOrShuffle(level);
        }
    }

    /**
     * 玩家登录时同步数据到客户端
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncAllDataToClient(player);
        player.refreshDimensions();
        ModMessages.sentToPlayer(new RefreshScaleS2CPacket(), player);

        // update cached attack type
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(PlayerAbility::updateBestAttackType);

        // 添加永久模块
        player.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                effectModules -> addPermanentModules(effectModules.getTriggerModules()));
    }

    public static void syncAllDataToClient(ServerPlayer player){
        syncSetDataToClient(player);
        syncPillDataToClient(player);
        syncItemDataToClient(player);
    }

    /**
     * 同步数据到客户端
     */
    public static void syncSetDataToClient(ServerPlayer player){
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    Map<ResourceLocation, Integer> map = playerPassiveItem.getSetCountMap();

                    IForgeRegistry<SetAbility> registry =
                            RegistryManager.ACTIVE.getRegistry(ModSetAbility.SET_ABILITY_KEY);
                    if (registry == null) return;

                    for (Map.Entry<ResourceLocation, Integer> entry : map.entrySet()) {
                        SetAbility ability = registry.getValue(entry.getKey());
                        if (ability == null) return;

                        ModMessages.sentToPlayer(new SetCountSyncS2CPacket(ability.getId(), entry.getValue()), player);
                    }
                }
        );
    }
    public static void syncPillDataToClient(ServerPlayer player){
        player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                playerItemUseRecord -> {
                    Map<Integer, ResourceLocation> map = Map.copyOf(playerItemUseRecord.getPillEffectMap());
                    for (Map.Entry<Integer, ResourceLocation> entry : map.entrySet()) {
                        ModMessages.sentToPlayer(new PillRecordsSyncS2CPacket(entry.getKey(), entry.getValue()), player);
                    }
                }
        );
    }
    public static void syncItemDataToClient(ServerPlayer player) {
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    Map<Integer, Integer> items = playerPassiveItem.getItemCountMapFromAll(player);
                    ModMessages.sentToPlayer(new PassiveItemMapSyncS2CPacket(items), player);
                });
    }

    /**
     * 首次给玩家创建Capability相关数据
     */
    @SubscribeEvent
    public static void onAttachCapability(AttachCapabilitiesEvent<Entity> event){
        if(event.getObject() instanceof Player){
            //passive item
            if(!event.getObject().getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_passive_item_cap"), new PlayerPassiveItemProvider());
            }//stat manager
            if(!event.getObject().getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_stat_manager_cap"), new PlayerStatModifierProvider());
            }//ability
            if(!event.getObject().getCapability(PlayerAbilityProvider.PLAYER_ABILITY).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_ability_cap"), new PlayerAbilityProvider());
            }//swallowedTrinkets
            if(!event.getObject().getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_swallowed_trinkets_cap"), new PlayerSwallowedTrinketsProvider());
            }//itemPools
            if(!event.getObject().getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_item_pools_cap"), new PlayerItemPoolsProvider());
            }//itemRecords
            if(!event.getObject().getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_item_use_record_cap"), new PlayerItemUseRecordProvider());
            }
        }
        if (event.getObject() instanceof LivingEntity){
            if(!event.getObject().getCapability(EntityEffectProvider.ENTITY_EFFECT_CAP).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "entity_effect_cap"), new EntityEffectProvider());
            }
            if(!event.getObject().getCapability(EffectModulesProvider.EFFECT_MODULES).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect_module_cap"), new EffectModulesProvider());
            }
            if(!event.getObject().getCapability(ExtraDataProvider.EXTRA_DATA_CAP).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "extra_data_cap"), new ExtraDataProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event){
        if(event.isWasDeath()){
            // 玩家的cap信息会在死亡后删除。需要用到reviveCaps恢复。复制完后再删除
            event.getOriginal().reviveCaps();
            // passive item
            event.getOriginal().getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            // stat manager
            event.getOriginal().getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore, event.getEntity());
                });
            });
            // ability
            event.getOriginal().getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            // swallowed trinkets
            event.getOriginal().getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            // item Pools
            event.getOriginal().getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerItemPoolsProvider.PLAYER_ITEM_POOL).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            // item records
            event.getOriginal().getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            // effect
            event.getOriginal().getCapability(EntityEffectProvider.ENTITY_EFFECT_CAP).ifPresent(oldStore -> {
                event.getEntity().getCapability(EntityEffectProvider.ENTITY_EFFECT_CAP).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
            // trigger modules
            event.getOriginal().getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(oldStore -> {
                event.getEntity().getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);

                    // 给玩家添加默认模块
                    addPermanentModules(newStore.getTriggerModules());
                });
            });
            // extra data
            event.getOriginal().getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(oldStore -> {
                event.getEntity().getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });


            event.getOriginal().invalidateCaps();
        }
    }

    /** 给玩家添加默认模块 */
    private static void addPermanentModules(TriggerModuleQueue queue){
        queue.addIfNotExist(ModTriggerModule.HIGH_PRIORITY_PLAYER_PERMANENT_MODULE.getId(), 1);
        queue.addIfNotExist(ModTriggerModule.PLAYER_PERMANENT_MODULE.getId(), 1);
    }


    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        // pill
        new PillShuffleCmd(event.getDispatcher());
        new PillTriggerByEffectCmd(event.getDispatcher());

        // item
        new ItemClearCmd(event.getDispatcher());
        new ItemGetCmd(event.getDispatcher());
        new ItemSpawnCmd(event.getDispatcher());

        // player
        new PlayerResetDataCmd(event.getDispatcher());
        new PlayerRefreshPoolCmd(event.getDispatcher());

        // trinket
        new TrinketClearSwallowedCmd(event.getDispatcher());
        new TrinketSetEnchanted(event.getDispatcher());


        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingAddEffect(MobEffectEvent.Added event) {
        // 获取受到效果的实体
        LivingEntity affectedEntity = event.getEntity();
        // 获取效果的来源（伤害来源）
        Entity source = event.getEffectSource();
        // 检查是否是目标药水效果
        if (event.getEffectInstance().getEffect() == ModEffects.POISON.get()) {
            // 确保来源是实体且存在
            if (source instanceof LivingEntity damageSourceEntity) {
                // 获取伤害来源的攻击伤害属性
                AttributeInstance attackDamage = damageSourceEntity.getAttribute(Attributes.ATTACK_DAMAGE);

                if (attackDamage != null) {
                    // 获取攻击伤害值
                    double damageValue = attackDamage.getValue();
                    // 保存
                    affectedEntity.getCapability(EntityEffectProvider.ENTITY_EFFECT_CAP).ifPresent(
                            entityEffect -> entityEffect.setSourceDamage(EffectManager.POISON.getId(),
                                    damageValue)
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();

        // 易伤
        if (victim.hasEffect(ModEffects.VULNERABLE.get())){
            int level = victim.getEffect(ModEffects.VULNERABLE.get()).getAmplifier() + 1;
            float newDamage = event.getAmount() * (1 + 0.3f * level);
            event.setAmount(newDamage);
        }
        // 金化
        if (victim.hasEffect(ModEffects.GOLDEN.get())){
            float newDamage = event.getAmount() * 2;
            event.setAmount(newDamage);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event){
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.GOLDEN.get())){
            LootHelper.spawnLootAtPos(entity, entity.position(), ModLootTables.RANDOM_COINS,
                    entity.getRandom().nextInt(0,3));
        }
    }


    @SubscribeEvent
    public static void onEntityKnockback(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = entity.getLastDamageSource();

        if (source == null) return;

        if (source.is(ModDamageType.LASER)) {
            event.setCanceled(true);
            return;
        }

        if (entity instanceof Player player){
            // 神圣护盾
            if (player.hasEffect(ModEffects.HOLY_SHIELD.get())){
                event.setCanceled(true);
            }
        }
    }


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isHoldingIsaacHead(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (isHoldingIsaacHead(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onRightClickEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isHoldingIsaacHead(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }
    }

    private static boolean isHoldingIsaacHead(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof IsaacHead)
                return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        var player = event.getEntity();

        double bonus = player.getAttributeValue(ModAttributes.BLOCK_BREAKING_SPEED.get());

        // 计算最终破坏速度
        float baseSpeed = event.getNewSpeed();
        float finalSpeed = (float) (baseSpeed + bonus);

        event.setNewSpeed(finalSpeed);
    }


    @SubscribeEvent
    public static void onCurioUnequipEvent(CurioUnequipEvent event){
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;

        // item类中的onUnequip方法获取到的不是原stack，故而需要通过事件修改
        ItemStack stack = event.getStack();

        if (stack.getItem() instanceof IIsaacCuriosItem ii){
            IIsaacCuriosItem.setOnCurios(stack, false);

            if (ii instanceof FoodPassiveItem){
                FoodPassiveItem.setBingeEater(stack, false);
            }
        }

    }


    // 临时流浪商人交易系统
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof WanderingTrader trader) {
            // if modified skip
            if (trader.getPersistentData().getBoolean("IsaacDisasterWanderingTraderModified")) return;
            MerchantOffers offers = trader.getOffers();

            // 添加额外交易
            addMandatoryTrades(offers, trader);

            trader.getPersistentData().putBoolean("IsaacDisasterWanderingTraderModified", true);
        }
    }

    private static void addMandatoryTrades(MerchantOffers offers, WanderingTrader trader) {
        // 获取货币物品
        String moneyId = Config.COIN_TIER_1_ID.get();
        Item money = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(moneyId));
        if (money == null) return;

        var random = trader.getRandom();

        // 普通交易（固定出现）
        offers.add(createOffer(Items.EMERALD, 1, money, 1, 999, 0.05f));
        offers.add(createOffer(money, 8, ModItems.BOMB.get(), 1, 2, 0.05f));
        offers.add(createOffer(money, 8, ModItems.KEY.get(), 1, 2, 0.05f));
        offers.add(createOffer(money, 8, ModItems.GRAB_BAG.get(), 1, 2, 0.05f));
        offers.add(createOffer(money, 5, ModItems.RED_HEART.get(), 1, 2, 0.05f));
        offers.add(createOffer(money, 5, ModItems.SOUL_HEART.get(), 1, 2, 0.05f));

        // 稀有交易（随机选取两个 IsaacItem）
        List<Item> items = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item instanceof IsaacItem)
                .toList();

        offers.add(createOffer(money, 30, items.get(random.nextInt(items.size())), 1, 1, 0.05f));
        offers.add(createOffer(money, 30, items.get(random.nextInt(items.size())), 1, 1, 0.05f));
    }

    /**
     * 创建一个 MerchantOffer 对象
     * @param money          货币物品
     * @param costAmount     货币数量
     * @param resultItem     出售的物品
     * @param resultCount    出售物品的数量
     * @param maxUses        交易可用次数
     * @param priceMultiplier 价格乘数（影响二次购买时的涨价幅度）
     */
    private static MerchantOffer createOffer(Item money, int costAmount, Item resultItem,
                                             int resultCount, int maxUses, float priceMultiplier) {
        return new MerchantOffer(
                new ItemStack(money, costAmount),
                new ItemStack(resultItem, resultCount),
                maxUses,
                5,   // 经验奖励
                priceMultiplier
        );
    }
}
