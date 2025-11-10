package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;

public class Battery extends Pickup implements ICommonPickup, IUsablePickup {
    public Battery(Properties pProperties, int itemId) {
        super(pProperties, itemId);
    }
}
