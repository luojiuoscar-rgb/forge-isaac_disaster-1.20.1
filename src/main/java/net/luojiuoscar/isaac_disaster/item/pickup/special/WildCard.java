package net.luojiuoscar.isaac_disaster.item.pickup.special;

import net.luojiuoscar.isaac_disaster.item.item.IIgnoreRecord;
import net.luojiuoscar.isaac_disaster.item.pickup.Card;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.minecraftforge.registries.RegistryObject;

public class WildCard extends Card implements IIgnoreRecord {
    public WildCard(Properties pProperties, RegistryObject<PickupAbility> ability) {
        super(pProperties, ability);
    }
}
