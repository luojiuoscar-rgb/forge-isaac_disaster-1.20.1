package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IChargeableAttack {

    void onPressed(ServerPlayer player);

    void onReleased(ServerPlayer player);

    int getTargetValue(Player player);
}
