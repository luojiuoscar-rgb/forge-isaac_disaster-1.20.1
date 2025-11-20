package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.manager.attack.IAttackType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PlayerPerformAttackEvent extends Event {
    private final Player player;
    private int attackTypeId;
    private final IAttackType.AttackContext context;

    public PlayerPerformAttackEvent(Player player, int attackTypeId, IAttackType.AttackContext context) {
        this.player = player;
        this.attackTypeId = attackTypeId;
        this.context = context;
    }

    public Player getPlayer() {
        return player;
    }

    public int getAttackTypeId() { return attackTypeId; }

    public void setAttackTypeId(int input){
        this.attackTypeId = input;
    }

    public IAttackType.AttackContext getContext(){
        return context;
    }
}
