package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.*;

import java.util.List;

public class TheWafer implements ITriggerModule {
    private static final CompositeTrigger triggers = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.THE_WAFER)
    ));

    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }

    @Override
    public double getPriority(){
        return TriggerModulePriority.LOWEST.priority;
    }
}
