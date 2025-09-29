package net.luojiuoscar.isaac_disaster.isaac.active_item;

import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.DirectObtainPassiveItemC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.ObtainPassiveItemC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.RemovePassiveItemFromIdC2SPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.List;


public interface ActiveItem {
    /**
     * 获取道具ID
     */
    int getItemId();

    /**
     * 道具的冷却
     */
    int getCd();

    void onTriggeredEffect(Player player);


    /**
     * 使用时的音效
     */
    default void onTriggerSound(Player player) {
        SoundEvent defaultSound = SoundEvents.PLAYER_LEVELUP;
        player.playSound(defaultSound, 1.0F, 1.0F);
    }


    default void onRechargeSound(Player player){

    }
}
