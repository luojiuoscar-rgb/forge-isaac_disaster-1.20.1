package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;

public class Bomb extends Pickup implements ICommonPickup, IUsablePickup {
    public Bomb(Properties pProperties, int itemId) {
        super(pProperties, itemId);
    }
}
