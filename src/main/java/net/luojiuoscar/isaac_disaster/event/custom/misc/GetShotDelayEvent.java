package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class GetShotDelayEvent extends Event {
    private final Player player;
    private final double originalDelay;
    private double delay;

    public GetShotDelayEvent(Player player, double delay) {
        this.player = player;
        this.originalDelay = delay;
        this.delay = delay;
    }

    public double getDelay() {
        return delay;
    }

    public double getOriginalDelay() {
        return originalDelay;
    }

    public Player getPlayer() {
        return player;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }
}
