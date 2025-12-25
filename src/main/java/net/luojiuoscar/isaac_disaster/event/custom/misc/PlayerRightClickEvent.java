package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PlayerRightClickEvent extends Event {
    private final ServerPlayer player;
    private final boolean onPressed;
    private final boolean onReleased;

    public PlayerRightClickEvent(ServerPlayer player, boolean onPressed, boolean onReleased) {
        this.player = player;
        this.onPressed = onPressed;
        this.onReleased = onReleased;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public boolean isOnPressed() {
        return onPressed;
    }

    public boolean isOnReleased() {
        return onReleased;
    }
}
