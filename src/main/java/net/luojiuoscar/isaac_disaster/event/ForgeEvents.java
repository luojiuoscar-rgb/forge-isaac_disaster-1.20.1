package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffectProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.*;
import net.luojiuoscar.isaac_disaster.commands.*;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.IIsaacCuriosItem;
import net.luojiuoscar.isaac_disaster.item.item.custom.FoodPassiveItem;
import net.luojiuoscar.isaac_disaster.item.pickup.special.IsaacHead;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.luojiuoscar.isaac_disaster.manager.data.PillShuffleData;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemMapSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.PillRecordsSyncS2CPacket;
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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.server.command.ConfigCommand;
import top.theillusivec4.curios.api.event.CurioUnequipEvent;

import java.util.Map;

import static net.luojiuoscar.isaac_disaster.IsaacDisaster.MOD_ID;


@Mod.EventBusSubscriber(modid = MOD_ID)
public class ForgeEvents {
    // 世界加载事件
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PillShuffleData data = PillShuffleData.get(level);

            // 首次世界无数据时进行一次shuffle
            if (data.getPillEffectMap().isEmpty()) {
                PillEffectManager.getInstance().shufflePills(level);
            }
        }
    }

    /**
     * 玩家登录时同步数据到客户端
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncAllDataToClient(player);

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
        new ClearPassiveItemsCommand(event.getDispatcher());
        new HasItemCommand(event.getDispatcher());
        new GetItemCountCommand(event.getDispatcher());
        new GetFlyCommand(event.getDispatcher());
        new ShufflePillCommand(event.getDispatcher());
        new TriggerPillEffectCommand(event.getDispatcher());
        new ResetPlayerCommand(event.getDispatcher());
        new ClearSwallowedTrinkets(event.getDispatcher());
        new SetTrinketEnchanted(event.getDispatcher());
        new AddTrinketSlotsCommand(event.getDispatcher());
        new GetTrinketsCommand(event.getDispatcher());
        new RefreshItemPoolCommand(event.getDispatcher());


        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event){
        // 检查攻击者是否为玩家
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(player instanceof ServerPlayer)) return;


        // 有4.5伏特时，将伤害转化为充能
        if (PlayerHelper.hasItem(ItemId.VOLT_9.getId(), (ServerPlayer) player)) return;


        // 遍历玩家所有物品槽位
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // 检查物品是否为NormalActiveItem类型且不为空
            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem) {
                int rate = Config.ACTIVE_ITEM_DURABILITY_RESTORE_RATE.get() / 2;
                // 近战伤害获取额外充能数值
                if (event.getSource().is(DamageTypes.PLAYER_ATTACK)){
                    rate *= 4;
                }
                // 充电（传入蓄电池参数）
                PlayerHelper.chargeItem(stack,
                        Math.max((int) event.getAmount() * rate, 1),
                        PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), (ServerPlayer) player));
            }
        }
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


    /**
     * 在未来版本被移除的事件
     * 用于控制实际碰撞箱大小
     */
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.getAttributes() == null) return;

        var attr = entity.getAttribute(ModAttributes.SCALE.get());
        if (attr == null) return;

        double scale = attr.getValue();

        // 基于实体类型的原始尺寸
        EntityDimensions base = entity.getType().getDimensions();

        // 计算新的绝对尺寸
        EntityDimensions newSize = EntityDimensions.scalable(
                (float) (base.width * scale),
                (float) (base.height * scale)
        );
        float baseEyeHeight = entity.getEyeHeight(event.getPose(), base);
        float scaledEyeHeight = (float) (baseEyeHeight * scale);

        // 应用
        event.setNewSize(newSize, true);
        event.setNewEyeHeight(scaledEyeHeight);
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
}
