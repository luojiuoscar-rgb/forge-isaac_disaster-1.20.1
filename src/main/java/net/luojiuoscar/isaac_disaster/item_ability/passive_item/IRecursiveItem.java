package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.minecraft.world.entity.player.Player;

public interface IRecursiveItem extends IPassiveItem{

    /**
     * @return 周期性道具的周期间隔
     */
    int getTickInterval();

    /**
     * 循环效果
     */
    void recursiveEffect(Player player);
}
