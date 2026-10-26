package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class IsaacGetBulletCountEvent extends Event {
    private final Player player;
    private final AttackType attackType;
    private int count;

    public IsaacGetBulletCountEvent(Player player, AttackType attackType, int baseCount) {
        this.player = player;
        this.attackType = attackType;
        this.count = baseCount;
    }

    public Player getPlayer() {
        return player;
    }

    public AttackType getAttackType() {
        return attackType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
