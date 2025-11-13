package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;

/**
 * 有n%概率射出特殊子弹的类型（非全局修改）
 * 或在某些情况下射出新贴图的类型
 */
public interface ISpecialTypeBulletPassiveItem {

    void onShoot(PlayerPerformAttackEvent event);
}
