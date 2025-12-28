package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class RightClickTickEvent extends Event {
    private final ServerPlayer player;

    public RightClickTickEvent(ServerPlayer player){
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
