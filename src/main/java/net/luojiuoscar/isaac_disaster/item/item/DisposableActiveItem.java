package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.minecraftforge.registries.RegistryObject;

public class DisposableActiveItem extends ActiveItem{

    public DisposableActiveItem(Properties properties, int chargePerUse, int maxCharge, RegistryObject<ActiveAbility> ability) {
        super(properties, chargePerUse, maxCharge, ability);
    }
}
