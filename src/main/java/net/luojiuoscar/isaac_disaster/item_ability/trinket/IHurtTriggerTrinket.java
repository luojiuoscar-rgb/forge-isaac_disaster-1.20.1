package net.luojiuoscar.isaac_disaster.item_ability.trinket;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface IHurtTriggerTrinket extends ITriggerTrinket{
    default void onHurt(Player player, Entity attacker, boolean isEnchanted) {
        triggerWithChance(player, () -> {
            handleHurtEffect(player, attacker, isEnchanted);
        }, isEnchanted);
    }

    void handleHurtEffect(Player player, Entity target, boolean isEnchanted);

    boolean isPunishType();
}
