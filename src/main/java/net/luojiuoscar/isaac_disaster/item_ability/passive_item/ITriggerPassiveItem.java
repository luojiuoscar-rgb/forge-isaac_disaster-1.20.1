package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;


public interface ITriggerPassiveItem extends IPassiveItem{
    double getTriggerChance(Player player);

    default double getPlayerLuck(Player player){
        AttributeInstance instance = player.getAttribute(Attributes.LUCK);
        if (instance == null) return 0;
        return instance.getValue();
    }

    /**
     * 概率触发处理器
     *
     * @param player 玩家
     * @param action 触发时执行的动作
     */
    default void triggerWithChance(Player player, Runnable action) {
        if (player.level().isClientSide()) return;
        if (player.getRandom().nextDouble() < getTriggerChance(player)) {
            action.run();
        }
    }
}