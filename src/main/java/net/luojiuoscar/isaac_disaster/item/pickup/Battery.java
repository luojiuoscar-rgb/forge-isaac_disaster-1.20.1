package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.minecraftforge.registries.RegistryObject;

public class Battery extends Pickup implements ICommonPickup, IUsablePickup {
    public Battery(Properties pProperties, RegistryObject<PickupAbility> ability) {
        super(pProperties, ability);
    }
}
