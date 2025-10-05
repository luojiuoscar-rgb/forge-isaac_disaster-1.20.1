package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * 触发型被动道具接口，处理由特定游戏事件触发的效果
 */
public interface ITriggerPassiveItem extends IPassiveItem{
    /**
     * 获取触发概率（double 0.0-1.0）
     */
    double getTriggerChance(Player player);

    default double getPlayerLuck(Player player){
        return player.getAttribute(Attributes.LUCK).getValue();
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
}