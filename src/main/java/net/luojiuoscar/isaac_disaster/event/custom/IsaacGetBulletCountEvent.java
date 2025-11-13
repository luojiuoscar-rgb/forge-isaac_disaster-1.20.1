package net.luojiuoscar.isaac_disaster.event.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class IsaacGetBulletCountEvent extends Event {
    private final Player player;
    private int count;

    public IsaacGetBulletCountEvent(Player player, int baseCount) {
        this.player = player;
        this.count = baseCount;
    }

    public Player getPlayer() {
        return player;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
