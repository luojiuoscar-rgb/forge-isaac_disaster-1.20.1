package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;

public class Sack extends Pickup implements IUsablePickup {
    public Sack(Properties pProperties, int itemId) {
        super(pProperties, itemId);
    }
}
