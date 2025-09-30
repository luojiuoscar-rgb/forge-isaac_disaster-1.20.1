package net.luojiuoscar.isaac_disaster.isaac.active_item;


import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.item.custom.NormalActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.UseActiveItemC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.UseActiveItemS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public interface ActiveItem {
    /**
     * 获取物品ID
     */
    int getItemId();

    

    /**
     * 收到客户端使用物品时
     */
    default void onReceiveC2SPacket(ServerPlayer player, int itemId){
        player.sendSystemMessage(Component.literal("收到包裹"));
        // 执行触发效果
        AtomicInteger carBatteryCount = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    carBatteryCount.set(playerPassiveItem.getItemCount(ItemId.CAR_BATTERY.getId()));
                });
        if (carBatteryCount.get() != 0){
            onTriggerEffectStronger(player);
        }else {
            onTriggeredEffect(player);
        }

        // 修改服务器端的物品耐久度
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        NormalActiveItem item = (NormalActiveItem) stack.getItem();

        int currentDamage = stack.getDamageValue();
        int newDamage = Math.min(
                currentDamage + item.getDamagePerUse(player)
                , stack.getMaxDamage() - 1);
        stack.setDamageValue(newDamage);

        // 设置0.5秒的冷却
        player.getCooldowns().addCooldown(item, 10);


        // 发送使用物品效果以触发客户端效果
        ModMessages.sentToPlayer(new UseActiveItemS2CPacket(itemId), player);
    }


    /**
     * 触发效果
     */
    void onTriggeredEffect(ServerPlayer player);

    /**
     * 触发更强效果时的效果
     */
    void onTriggerEffectStronger(ServerPlayer player);


    /**
     * 使用时的客户端效果
     */
    default void onUseClientEffect(Player player){
        onTriggerSound(player);
    }

    /**
     * 使用时的音效
     */
    default void onTriggerSound(Player player) {
        SoundEvent defaultSound = SoundEvents.PLAYER_LEVELUP;
        player.playSound(defaultSound, 1.0F, 1.0F);
    }

    /**
     * 充能时的音效
     */
    default void onRechargeSound(Player player){

    }

    /**
     * 获取物品实例
     */
    ItemStack getItemStack();

    /**
     * 获取名称
     */
    default Component getDisplayName(){
        return getItemStack().getDisplayName();
    }

    /**
     * 获取描述文本(lore)
     */
    List<Component> getDescription();
}
    