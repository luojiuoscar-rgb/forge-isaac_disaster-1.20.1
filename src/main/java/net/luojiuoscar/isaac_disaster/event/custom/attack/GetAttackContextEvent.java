package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class GetAttackContextEvent extends Event {
    private final Player player;
    private final AttackContext attackContext;

    public GetAttackContextEvent(Player player, AttackContext attackContext) {
        this.player = player;
        this.attackContext = attackContext;
    }

    public Player getPlayer() {
        return player;
    }

    public AttackContext getContext() {
        return attackContext;
    }
}
