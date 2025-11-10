package net.luojiuoscar.isaac_disaster.event.custom;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class PickupUseEvent extends Event {
    private final Player player;
    private final ItemStack stack;

    public PickupUseEvent(Player player, ItemStack stack) {
        this.player = player;
        this.stack = stack;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getStack() {
        return stack;
    }
}
