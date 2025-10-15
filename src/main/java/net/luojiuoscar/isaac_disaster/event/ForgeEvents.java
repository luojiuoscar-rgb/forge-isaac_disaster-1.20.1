package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffectProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbility;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.commands.*;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.pickup.IOnUse;
import net.luojiuoscar.isaac_disaster.item.pickup.IsaacHead;
import net.luojiuoscar.isaac_disaster.item.pickup.Pickup;
import net.luojiuoscar.isaac_disaster.manager.EffectNameManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.*;
import net.luojiuoscar.isaac_disaster.data.PillShuffleData;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 首次给玩家创建Capability相关数据
     */
    @SubscribeEvent
    public static void onAttachCapabilityPlayer(AttachCapabilitiesEvent<Entity> event){
        if(event.getObject() instanceof Player){
            //passive item
            if(!event.getObject().getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_passive_item_cap"), new PlayerPassiveItemProvider());
            }
            //stat manager
            if(!event.getObject().getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_stat_manager_cap"), new PlayerStatModifierProvider());
            }//ability
            if(!event.getObject().getCapability(PlayerAbilityProvider.PLAYER_ABILITY).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(MOD_ID, "player_ability_cap"), new PlayerAbilityProvider());
            }
        }
    }


    /**
     * 死后保留
     */
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
                    newStore.copyFrom(oldStore, event.getEntity());
                });
            });

            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        new ShowPassiveItemsCommand(event.getDispatcher());
        new ClearPassiveItemsCommand(event.getDispatcher());
        new HasItemCommand(event.getDispatcher());
        new GetItemCountCommand(event.getDispatcher());
        new GetFlyCommand(event.getDispatcher());
        new ShufflePillCommand(event.getDispatcher());
        new TriggerPillEffectCount(event.getDispatcher());
        new ResetPlayerCommand(event.getDispatcher());


        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if(player.level().isClientSide()) return;


        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(event.getHand());
        if(stack.isEmpty()) return;

        // 判断物品类型
        if(stack.getItem() instanceof ActiveItem item){
            RCActiveItem(player, item, stack, hand);
        }
        else if(stack.getItem() instanceof PassiveItem item){
            player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                    playerPassiveItem -> {playerPassiveItem.addItem((ServerPlayer) player, item.getItemId(), hand);
                    });
        }
        else if(stack.getItem() instanceof IOnUse && stack.getItem() instanceof Pickup item){
            PickupManager.getInstance().getItemFromId(item.getItemId()).onUse(player, stack, hand);
        }
    }

    // right-clicked active item
    private static void RCActiveItem(Player player, ActiveItem item, ItemStack stack, InteractionHand hand){
        // 如果当前物品的耐久度不足且没有过载则无法使用物品
        if (!ActiveItem.getOverCharged(stack) &&
                stack.getMaxDamage() - stack.getDamageValue() < item.getDamagePerUse(player)){
            return;
        }

        // 触发使用效果
        if (!player.level().isClientSide()) {
            // 服务端效果
            ActiveItemManager.getInstance().getItemFromId(item.getItemId()).onUse(player, hand);
            ModMessages.sentToPlayer(new UseActiveItemS2CPacket(item.getItemId()), (ServerPlayer) player);
        }
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
                // 充电（传入蓄电池参数）
                ActiveItem.modifyCharge(stack, Math.max((int) event.getAmount() * 2, 1),PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), (ServerPlayer) player));
            }
        }
    }

    // 监听实体创建时的事件，附加 Capability
    @SubscribeEvent
    public static void onAttachCapabilitiesEntity(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();

        // 只给生物实体（LivingEntity）附加 Capability（过滤非生物实体，如物品、方块实体等）
        if (entity instanceof LivingEntity) {
            // 定义一个唯一的标识符（避免与其他模组冲突）
            ResourceLocation capabilityId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "entity_effect_cap");

            // 检查实体是否已附加该 Capability，避免重复添加
            if (!event.getObject().getCapability(EntityEffectProvider.ENTITY_CAP).isPresent()) {
                // 创建 Capability 提供者实例
                EntityEffectProvider provider = new EntityEffectProvider();

                // 附加 Capability 到实体
                event.addCapability(capabilityId, provider);
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
        if (event.getEffectInstance().getEffect() == ModEffects.ISAAC_POISON.get()) {
            // 确保来源是实体且存在
            if (source instanceof LivingEntity damageSourceEntity) {
                // 获取伤害来源的攻击伤害属性
                AttributeInstance attackDamage = damageSourceEntity.getAttribute(Attributes.ATTACK_DAMAGE);

                if (attackDamage != null) {
                    // 获取攻击伤害值
                    double damageValue = attackDamage.getValue();
                    // 保存
                    affectedEntity.getCapability(EntityEffectProvider.ENTITY_CAP).ifPresent(
                            entityEffect -> entityEffect.setSourceDamage(EffectNameManager.POISON,
                                    damageValue)
                    );
                }
            }
        }
    }


    /**
     * 玩家登录时同步数据到客户端
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 同步套装
            syncSetDataToClient(serverPlayer);
            // 胶囊相关
            syncPillDataToClient(serverPlayer);
            syncPillQualityToClient(serverPlayer);
            // 从服务端Capability获取数据
            syncItemDataToClient(ItemId.CAR_BATTERY.getId(), serverPlayer);
            syncItemDataToClient(ItemId.BLOOD_OF_THE_MARTYR.getId(), serverPlayer);
            syncItemDataToClient(ItemId.PHD.getId(), serverPlayer);
            syncItemDataToClient(ItemId.FALSE_PHD.getId(), serverPlayer);
        }
    }

    /**
     * 同步数据到客户端
     */
    public static void syncSetDataToClient(ServerPlayer player){
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    Map<Integer,Integer> map = Map.copyOf(playerPassiveItem.getSetCountMap());
                    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                        ModMessages.sentToPlayer(new SetCountSyncS2CPacket(entry.getKey(), entry.getValue()), player);
                    }
                }
        );
    }
    public static void syncPillDataToClient(ServerPlayer player){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    Map<Integer,Integer> map = Map.copyOf(playerAbility.getPillRecordsMap());
                    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                        ModMessages.sentToPlayer(new PillRecordsSyncS2CPacket(entry.getKey(), entry.getValue()), player);
                    }
                }
        );
    }
    public static void syncPillQualityToClient(ServerPlayer player){
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    int quality = playerAbility.getPillQuality();
                    ModMessages.sentToPlayer(new PillQualitySyncS2CPacket(quality), player);
                }
        );
    }
    public static void syncItemDataToClient(int ItemId, ServerPlayer player) {
        AtomicInteger count = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> count.set(playerPassiveItem.getItemCount(ItemId))
        );
        ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId, count.get()), player);
    }


    /**
     * 监听爆炸事件
     */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        // 获取爆炸源实体
        Entity source = event.getExplosion().getDirectSourceEntity();
        Level level = event.getLevel();

        // 获取到tnt的owner属性；触发后续逻辑
        if (source instanceof IsaacBomb tnt) {
            LivingEntity owner = tnt.getOwner();
            if (owner instanceof ServerPlayer player){
                // 获取pos
                Vec3 pos = new Vec3(tnt.getX(), tnt.getY(), tnt.getZ());
                // bomber boy
                if(PlayerHelper.hasItem(ItemId.BOMBER_BOY.getId(), player)){
                    EntityHelper.BomberBoy(player, tnt, pos, level);
                }

                // scatter bomb
                if(PlayerHelper.hasItem(ItemId.SCATTER_BOMB.getId(), player)){
                    EntityHelper.ScatterBomb(player, tnt, pos, level);
                }

                // hot bomb
                if(PlayerHelper.hasItem(ItemId.HOT_BOMB.getId(), player)){
                    EntityHelper.HotBomb(player, tnt, pos, level);
                }
            }
        }
    }


    /**
     * 受伤
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        DamageSource source = event.getSource();

        double damage = event.getAmount();

        if (victim instanceof Player victimPlayer){
            // 死灵护盾
            if (victimPlayer.hasEffect(ModEffects.NECRONMICON_SHIELD.get()) && damage > 2){
                // 伤害来源不能是拥有死灵庇护的玩家；否则不生效
                if (!(attacker instanceof Player attackerplayer &&
                        attackerplayer.hasEffect(ModEffects.NECRONMICON_SHIELD.get()))){
                    // effect
                    ActiveItemManager.getInstance().getItemFromId(ItemId.THE_NECRONMICON.getId()).onTriggeredEffect(victimPlayer);
                    // remove 1 amplifier
                    PlayerHelper.removeAmplifier(victimPlayer, ModEffects.NECRONMICON_SHIELD.get());
                    // sounds
                    victimPlayer.level().playSound(null, victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ(),
                            ModSounds.BLACK_HEART_ACTIVE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
            // 神圣护盾
            if (victimPlayer.hasEffect(ModEffects.HOLY_SHIELD.get())){
                int amplifier = victimPlayer.getEffect(ModEffects.HOLY_SHIELD.get()).getAmplifier();

                event.setAmount(0.0f);
                victimPlayer.sendSystemMessage(Component.literal(""+(amplifier + 1) * StatManager.getHolyShieldStrength()));
                if (damage > (amplifier + 1) * StatManager.getHolyShieldStrength()){
                    // 只有伤害足够高的时候才移除护盾
                    PlayerHelper.removeAmplifier(victimPlayer, ModEffects.HOLY_SHIELD.get());
                    // sounds
                    victimPlayer.level().playSound(null, victimPlayer.getX(), victimPlayer.getY(), victimPlayer.getZ(),
                            ModSounds.HOLY_SHIELD_BROKE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
            // 圣饼
            if (PlayerHelper.hasItem(ItemId.THE_WAFER.getId(), (ServerPlayer) victimPlayer)){
                event.setAmount(event.getAmount() * 0.5f);
            }
            // 成人套装
            if (PlayerHelper.hasSet(SetId.ADULT.getId(), (ServerPlayer) victimPlayer)){
                victimPlayer.level().playSound(null, BlockPos.containing(victimPlayer.blockPosition().getCenter()),
                        ModSounds.STEVE_HURT_OLD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }

        }

        if (victim.hasEffect(ModEffects.VULNERABLE.get())){
            // 易伤
            int level = victim.getEffect(ModEffects.VULNERABLE.get()).getAmplifier() + 1;
            float newDamage = event.getAmount() * (1 + 0.3f * level);
            event.setAmount(newDamage);
        }
    }

    @SubscribeEvent
    public static void onEntityKnockback(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();

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
}
