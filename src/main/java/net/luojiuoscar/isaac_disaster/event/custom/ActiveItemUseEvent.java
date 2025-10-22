package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ActiveItemUseEvent extends Event {
    private final Player player;
    private final ActiveItem activeItem;
    private final ItemStack stack;

    public ActiveItemUseEvent(Player player, ActiveItem activeItem, ItemStack stack) {
        this.player = player;
        this.activeItem = activeItem;
        this.stack = stack;
    }

    public Player getPlayer() {
        return player;
    }

    public ActiveItem getActiveItem() {
        return activeItem;
    }

    public ItemStack getStack() {
        return stack;
    }
}
