package net.luojiuoscar.isaac_disaster.item_ability.trinket;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.List;

public interface ITriggerTrinket extends ITrinket{
    double getTriggerChance(Player player, List<ItemStack> stackList);

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
    default boolean triggerWithChance(Player player, Runnable action, List<ItemStack> stackList) {
        if (player.level().isClientSide()) return false;
        double random = player.getRandom().nextDouble();
        double target = getTriggerChance(player, stackList);
        if (random < target) {

            action.run();
            return true;
        }
        return false;
    }

    default void onTrigger(Player player, List<ItemStack> stackList){
        onTrigger(player, stackList, null);
    }
    default void onTrigger(Player player, List<ItemStack> stackList, @Nullable Event event) {
        triggerWithChance(player, () -> {
            handleTriggerEffect(player, stackList, event);
        }, stackList);
    }

    default void handleTriggerEffect(Player player, List<ItemStack> stackList, Event event){};

}
