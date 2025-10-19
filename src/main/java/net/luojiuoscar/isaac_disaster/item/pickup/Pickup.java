package net.luojiuoscar.isaac_disaster.item.pickup;

import net.minecraft.world.item.Item;

public class Pickup extends Item {
    private final int itemId;

    public Pickup(Properties pProperties, int itemId) {
        super(pProperties);
        this.itemId = itemId;
    }

    public int getPickupId() {
        return itemId;
    }
}
