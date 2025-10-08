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


    /**
     * 获取道具时的部分效果。不包含直接获取道具时的效果
     * 由服务端触发
     */
    void onObtain(Player player);


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
     * 直接给予道具时触发、不会生成部分掉落物、部分效果不触发
     * 由服务端触发
     */
    void onDirectObtain(Player player);


    default void onDirectObtainClient(Player player){

    }

    /**
     * 移除道具时触发
     */
    void onRemove(Player player);


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
