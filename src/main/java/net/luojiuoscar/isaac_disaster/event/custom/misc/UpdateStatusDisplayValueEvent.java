package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class UpdateStatusDisplayValueEvent extends Event {
    private final ServerPlayer player;
    private final PlayerStatModifier.StatInstance inst;


    public UpdateStatusDisplayValueEvent(ServerPlayer player, PlayerStatModifier.StatInstance inst){
        this.player = player;
        this.inst = inst;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public PlayerStatModifier.StatInstance getInst() {
        return inst;
    }
}
