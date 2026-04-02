package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;

import java.util.List;

public class LuckyRock implements ITriggerModule {
    private final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BREAK_BLOCK, ModAbilityEffects.LUCKY_ROCK)
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }
}
