package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffectProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.commands.ClearPassiveItemsCommand;
import net.luojiuoscar.isaac_disaster.commands.GetItemCountCommand;
import net.luojiuoscar.isaac_disaster.commands.HasItemCommand;
import net.luojiuoscar.isaac_disaster.commands.ShowPassiveItemsCommand;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.pickup.Pickup;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.EffectNameManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PickupManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.UseActiveItemS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

import java.util.concurrent.atomic.AtomicInteger;

import static net.luojiuoscar.isaac_disaster.IsaacDisaster.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class ModEvents {
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
                    newStore.copyFrom(oldStore, event.getEntity()); //同时传入新生成的玩家实例
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
        else if(stack.getItem() instanceof Pickup item){
            PickupManager.getInstance().getItemFromId(item.getItemId()).onUse(player, stack, hand);
        }
    }

    // right-clicked active item
    private static void RCActiveItem(Player player, ActiveItem item, ItemStack stack, InteractionHand hand){
        // 如果当前物品的耐久度不足且没有过载则无法使用物品
        if (!ActiveItem.getOverCharged(stack) &&
                stack.getMaxDamage() - stack.getDamageValue() < ActiveItem.getDamagePerUse(player)){
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
                            entityEffect -> entityEffect.setSourceDamage(EffectNameManager.ISAAC_POISON,
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
            // 从服务端Capability获取数据
            syncCarBatteryDataToClient(serverPlayer);
        }
    }

    /**
     * 同步电池数据到客户端
     */
    public static void syncCarBatteryDataToClient(ServerPlayer player) {
        // 车载电池
        AtomicInteger count = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> count.set(playerPassiveItem.getItemCount(ItemId.CAR_BATTERY.getId()))
        );
        ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.CAR_BATTERY.getId(), count.get()), player);
    }


    /**
     * 监听爆炸事件
     */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        LocalPlayer playerl = Minecraft.getInstance().player;
        assert playerl != null;
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

}
