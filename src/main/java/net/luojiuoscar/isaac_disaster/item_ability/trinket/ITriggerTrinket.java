package net.luojiuoscar.isaac_disaster.item_ability.trinket;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.mojang.text2speech.Narrator.LOGGER;

public interface ITriggerTrinket extends ITrinket{
    double getTriggerChance(Player player, boolean isEnchanted);

    default double getPlayerLuck(Player player){
        AttributeInstance instance = player.getAttribute(Attributes.LUCK);
        if (instance == null) return 0;
        return instance.getValue();
    }

    /**
     * 概率触发处理器
     * @param player 玩家
     * @param action 触发时执行的动作
     * @return 是否触发成功
     */
    default boolean triggerWithChance(Player player, Runnable action, boolean isEnchanted) {
        if (player.level().isClientSide()) return false;
        double random = player.getRandom().nextDouble();
        double target = getTriggerChance(player, isEnchanted);
        LOGGER.info("TRIGGER WITH CHANCE {}/{}", random, target);
        if (random < target) {

            action.run();
            return true;
        }
        return false;
    }
}
