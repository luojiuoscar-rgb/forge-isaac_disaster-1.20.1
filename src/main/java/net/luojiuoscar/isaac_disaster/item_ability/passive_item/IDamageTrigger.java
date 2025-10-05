package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface IDamageTrigger extends ITriggerPassiveItem {
    /**
     * 攻击实体时触发
     *
     * @param player 玩家
     * @param target 被攻击的实体
     */
    default void onAttackEntity(Player player, LivingEntity target) {
        triggerWithChance(player, () -> {
            // 具体触发逻辑由实现类完成
            handleAttackEntityEffect(player, target);
        });
    }

    // 以下为需要实现类具体实现的效果方法
    void handleAttackEntityEffect(Player player, LivingEntity target);
}
