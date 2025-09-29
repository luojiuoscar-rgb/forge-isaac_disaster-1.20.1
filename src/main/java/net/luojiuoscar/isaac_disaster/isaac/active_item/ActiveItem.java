package net.luojiuoscar.isaac_disaster.isaac.active_item;


import net.luojiuoscar.isaac_disaster.item.custom.NormalActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.UseActiveItemS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.List;



public interface ActiveItem {
    /**
     * 获取物品ID
     */
    int getItemId();

    /**
     * 收到客户端使用物品时
     */
    default void onReceiveC2SPacket(ServerPlayer player, int itemId){
        // 执行触发效果
        onTriggeredEffect(player);

        // 修改服务器端的使用次数&CD
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (stack.getItem() instanceof NormalActiveItem) {
            NormalActiveItem item = (NormalActiveItem) stack.getItem();
            // 通过 ItemStack 修改使用次数&CD
            item.modifyCurrentItemUseCount(stack, 1);
            item.resetCD(stack);

            // 只有该道具不在冷却列表中时才加入
            if (!ItemListManager.ACTIVE_ITEMS_IN_CD_LIST.contains(stack)) {
                ItemListManager.ACTIVE_ITEMS_IN_CD_LIST.add(stack);
            }
        }

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
    