package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;

import java.util.List;

public class Technology2 implements ITriggerModule {
    private final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.RIGHT_CLICK_TICK, ModAbilityEffects.SHOOT_LASER)
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }
}
