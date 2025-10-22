package net.luojiuoscar.isaac_disaster.item_ability.trinket;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
import java.util.List;

public interface IHurtTriggerTrinket extends ITriggerTrinket{
    default void onHurt(Player player, Entity attacker, List<ItemStack> stackList){
        onHurt(player, attacker, stackList, null);
    }
    default void onHurt(Player player, Entity attacker, List<ItemStack> stackList, @Nullable LivingHurtEvent event) {
        triggerWithChance(player, () -> {
            handleHurtEffect(player, attacker, stackList, event);
        }, stackList);
    }

    void handleHurtEffect(Player player, Entity attacker, List<ItemStack> stackList, LivingHurtEvent event);

    boolean isPunishType();
}
