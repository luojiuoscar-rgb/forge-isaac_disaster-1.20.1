package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;

import java.util.List;

public class CurseOfTheTower implements ITriggerModule {
    private final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModAbilityEffects.CURSE_OF_THE_TOWER)
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }
}
