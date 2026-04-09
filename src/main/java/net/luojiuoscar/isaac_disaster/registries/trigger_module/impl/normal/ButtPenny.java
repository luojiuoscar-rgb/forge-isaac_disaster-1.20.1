package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;

import java.util.List;

public class ButtPenny implements ITriggerModule {
    private static final CompositeTrigger triggers = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.PICKUP_ITEM, ModExecutableEffects.BUTT_PENNY)
    ));

    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }
}
