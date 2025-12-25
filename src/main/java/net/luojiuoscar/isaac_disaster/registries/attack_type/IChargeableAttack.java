package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.minecraft.world.entity.player.Player;

public interface IChargeableAttack {

    void onPressed(Player player, AttackContext context);

    void onReleased(Player player, AttackContext context);

    int getTargetValue(Player player);
}
