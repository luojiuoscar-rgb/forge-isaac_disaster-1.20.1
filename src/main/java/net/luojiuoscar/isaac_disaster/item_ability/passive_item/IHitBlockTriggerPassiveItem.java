package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;


public interface IHitBlockTriggerPassiveItem extends ITriggerPassiveItem {

    default void onHitBlock(Player player, Vec3 pos) {
        triggerWithChance(player, () -> {
            // 具体触发逻辑由实现类完成
            handleHitBlockEffect(player, pos);
        });
    }

    // 以下为需要实现类具体实现的效果方法
    void handleHitBlockEffect(Player player, Vec3 pos);
}
