package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.world.entity.player.Player;


/**
 * 有n%概率射出特殊子弹的类型（非全局修改）
 * 或在某些情况下射出新贴图的类型
 */
public interface INewBulletTypePassiveItem {

    default int getNewColor(){
        return ColorManager.COLOR_BASE;
    }
    default float getNewAlpha(){
        return 1.0f;
    }
    default int getNewFilter(){
        return ColorManager.FILTER_BASE;
    }

    void onShootEffect(Player player, IsaacBullet bullet);
}
