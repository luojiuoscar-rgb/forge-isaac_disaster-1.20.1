package net.luojiuoscar.isaac_disaster.item_ability.trinket;

import net.minecraft.world.entity.player.Player;

public interface IRecursiveTrinket extends ITrinket {

    int getTickInterval();

    void recursiveEffect(Player player, boolean isEnchanted);
}

