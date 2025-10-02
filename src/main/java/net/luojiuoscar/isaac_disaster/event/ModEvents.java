package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffect;
import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffectProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.commands.clearPassiveItemsCommand;
import net.luojiuoscar.isaac_disaster.commands.showPassiveItemsCommand;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.item.custom.NormalActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.EffectNameManager;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.UseActiveItemS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraftforge.server.command.ConfigCommand;
import org.lwjgl.system.linux.liburing.LibIOURing;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.text2speech.Narrator.LOGGER;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ModEvents {
    /**
     * 首次给玩家创建Capability相关数据
     */
    @SubscribeEvent
    public static void onAttachCapabilityPlayer(AttachCapabilitiesEvent<Entity> event){
        if(event.getObject() instanceof Player){
            //passive item
            if(!event.getObject().getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "player_passive_item_cap"), new PlayerPassiveItemProvider());
            }
            //stat manager
            if(!event.getObject().getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).isPresent()){
                event.addCapability(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "player_stat_manager_cap"), new PlayerStatModifierProvider());
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
        new showPassiveItemsCommand(event.getDispatcher());
        new clearPassiveItemsCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();

        // 判断物品是否为NormalActiveItem
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(event.getHand());
        if(stack.isEmpty() || !(stack.getItem() instanceof NormalActiveItem item)){
            return;
        }


        // 如果当前物品的耐久度不足且没有过载则无法使用物品
        if (!NormalActiveItem.getOverCharged(stack) &&
                stack.getMaxDamage() - stack.getDamageValue() < NormalActiveItem.getDamagePerUse(player)){
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
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // 有4.5伏特时，将伤害转化为充能
        AtomicInteger volt_4p5 = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> volt_4p5.set(playerPassiveItem.getItemCount(ItemId.VOLT_4P5.getId())));
        if (volt_4p5.get() <= 0){
            return;
        }

        // 是否有蓄电池
        AtomicInteger theBatteryCount = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> theBatteryCount.set(playerPassiveItem.getItemCount(ItemId.THE_BATTERY.getId())));
        // 遍历玩家所有物品槽位
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // 检查物品是否为NormalActiveItem类型且不为空
            if (!stack.isEmpty() && stack.getItem() instanceof NormalActiveItem) {
                // 充电
                NormalActiveItem.modifyCharge(stack, Math.max((int) event.getAmount() * 2, 1),theBatteryCount.get() > 0);
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
            ResourceLocation capabilityId = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "entity_effect_cap");

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
            LOGGER.info("NMAIWDJIOAWJD");
            // 确保来源是实体且存在
            if (source instanceof LivingEntity damageSourceEntity) {
                LOGGER.info("yes that's a entity");
                // 获取伤害来源的攻击伤害属性
                AttributeInstance attackDamage = damageSourceEntity.getAttribute(Attributes.ATTACK_DAMAGE);

                if (attackDamage != null) {
                    // 获取攻击伤害值
                    double damageValue = attackDamage.getValue();
                    LOGGER.info("damageValue: "+damageValue);
                    // 保存
                    affectedEntity.getCapability(EntityEffectProvider.ENTITY_CAP).ifPresent(
                            entityEffect -> entityEffect.setSourceDamage(EffectNameManager.ISAAC_POISON,
                                    damageValue)
                    );
                }
            }
        }
    }
}
