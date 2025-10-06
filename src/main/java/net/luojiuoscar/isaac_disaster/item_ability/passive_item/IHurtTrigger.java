package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface IHurtTrigger extends ITriggerPassiveItem {

    default void onHurt(Player player, LivingEntity attacker) {
        triggerWithChance(player, () -> {
            // 具体触发逻辑由实现类完成
            handleHurtEffect(player, attacker);
        });
    }

    // 以下为需要实现类具体实现的效果方法
    void handleHurtEffect(Player player, LivingEntity target);
}
