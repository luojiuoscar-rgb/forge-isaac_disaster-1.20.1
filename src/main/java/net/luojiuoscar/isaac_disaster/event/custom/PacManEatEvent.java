package net.luojiuoscar.isaac_disaster.event.custom;

import net.minecraftforge.eventbus.api.Event;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PacManEatEvent extends Event {

    private final Player player;
    private final LivingEntity eatenEntity;

    public PacManEatEvent(Player player, LivingEntity eatenEntity) {
        this.player = player;
        this.eatenEntity = eatenEntity;
    }

    public Player getPlayer() {
        return player;
    }

    public LivingEntity getEatenEntity() {
        return eatenEntity;
    }
}
