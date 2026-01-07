package net.luojiuoscar.isaac_disaster.block.custom;

import net.luojiuoscar.isaac_disaster.Config;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ItemDisplayContainerBlock {
    default void acquireItem(Player player, InteractionHand hand, ItemStack stack){
        player.setItemInHand(hand, stack);

        // 如果设置了自动使用，则在拾取的时候直接调用使用方法
        if (Config.AUTO_USE_PASSIVE_ITEM.get()){
            stack.use(player.level(), player, hand);
        }
    }

}
