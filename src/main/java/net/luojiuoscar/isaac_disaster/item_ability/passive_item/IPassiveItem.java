package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


import java.util.ArrayList;
import java.util.List;


public interface IPassiveItem {
    /**
     * 获取道具ID
     */
    int getItemId();


    /** 不会被remove的效果 */
    void onFirstObtain(Player player, boolean isPermanent);

    void onObtain(Player player, boolean isPermanent);


    /**
     * 移除道具时触发
     */
    void onRemove(Player player, boolean isPermanent);


    default void onObtainClient(Player player){
        //音效 需要在客户端执行
        onObtainSound(player);
    }

    /**
     * 获取道具时的默认音效，可修改
     */
    default void onObtainSound(Player player) {
        SoundEvent defaultSound = ModSounds.DEFAULT_OBTAIN_ITEM.get();
        player.playSound(defaultSound, 1.0F, 1.0F);
    }




    /**
     * 获取道具实例
     */
    ItemStack getItemStack();

    /**
     * 获取名称
     */
    default Component getDisplayName(){
        return getItemStack().getDisplayName();
    }

    /**
     * 获取lore
     */
    List<Component> getDescription();

    /**
     * 获取解释性信息
     */
    default List<Component> getExplain(){
        return new ArrayList<>();
    };

    default List<Component> getSynergyDescription(){
        return new ArrayList<>();
    }


}
