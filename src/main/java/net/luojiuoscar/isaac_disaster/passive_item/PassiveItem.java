/**
 * 被动道具接口，定义被动道具的核心行为和属性
 */
package net.luojiuoscar.isaac_disaster.passive_item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


import java.util.List;

public interface PassiveItem {
    //获取道具ID
    int getItemId();

    //获取道具时触发(包含生成、基于掉落物等)
    default void onObtain(Player player){
        //音效
        onObtainSound(player);
        //其他非直接获取时触发的效果
        obtainEffect(player);
        //直接获取时触发的效果
        onDirectObtain(player);
    };

    //直接给予道具时触发、不会生成部分掉落物、部分效果不触发
    default void onDirectObtain(Player player) {
        //移除道具时的效果
        directObtainEffect(player);
        //增加到道具列表
    };

    //移除道具时触发
    default void onRemove(Player player){
        //移除道具时的效果
        removeEffect(player);
        //从道具列表中删除
    };

    //获取道具时的默认音效；可修改
    default void onObtainSound(Player player) {
        SoundEvent defaultSound = SoundEvents.PLAYER_LEVELUP;
        player.playSound(defaultSound, 1.0F, 1.0F);
    }

    //获取道具时的部分效果。不包含直接获取道具时的效果
    void obtainEffect(Player player);

    //直接给予道具时触发、不会生成部分掉落物、部分效果不触发
    void directObtainEffect(Player player);

    //移除道具时的效果
    void removeEffect(Player player);

    //获取道具实例
    ItemStack getItemStack();

    default Component getDisplayName(){
        return getItemStack().getDisplayName();
    }

    default List<Component> getDescription(){
        return getItemStack().getTooltipLines(null, TooltipFlag.NORMAL);
    }

}
