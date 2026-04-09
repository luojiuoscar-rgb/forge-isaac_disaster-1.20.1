package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;

import java.util.List;

public class CursedPenny extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.PICKUP_ITEM, ModExecutableEffects.CURSED_PENNY)
    ));

    public CursedPenny() {
        super(TRIGGER);
    }
}
