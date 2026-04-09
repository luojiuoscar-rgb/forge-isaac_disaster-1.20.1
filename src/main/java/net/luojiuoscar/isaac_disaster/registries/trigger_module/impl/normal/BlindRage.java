package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModulePriority;

import java.util.List;

public class BlindRage implements ITriggerModule {
    private static final CompositeTrigger triggers = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.BLIND_RAGE)
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
