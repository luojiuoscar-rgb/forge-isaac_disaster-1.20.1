package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * 触发型被动道具接口，处理由特定游戏事件触发的效果
 */
public interface IDamageTriggerPItem {
    /**
     * 获取触发概率（double 0.0-1.0）
     */
    double getTriggerChance(Player player);

    default double getPlayerLuck(Player player){
        return player.getAttribute(Attributes.LUCK).getValue();
    }

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

    /**
     * 概率触发处理器
     * @param player 玩家
     * @param action 触发时执行的动作
     * @return 是否触发成功
     */
    default boolean triggerWithChance(Player player, Runnable action) {
        if (player.level().isClientSide()) return false; // 确保在服务端处理
        if (Math.random() <= getTriggerChance(player)) {
            action.run();
            return true;
        }
        return false;
    }

    // 以下为需要实现类具体实现的效果方法
    void handleAttackEntityEffect(Player player, LivingEntity target);
}