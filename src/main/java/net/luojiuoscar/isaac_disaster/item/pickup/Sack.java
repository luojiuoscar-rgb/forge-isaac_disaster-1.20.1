package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.IUsablePickup;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.minecraftforge.registries.RegistryObject;

public class Sack extends Pickup implements IUsablePickup {
    public Sack(Properties pProperties, RegistryObject<PickupAbility> ability) {
        super(pProperties, ability);
    }
}
