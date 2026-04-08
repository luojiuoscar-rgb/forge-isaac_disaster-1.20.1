package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModulePriority;

import java.util.List;

public class HighPriorityPlayerPermanentModule implements ITriggerModule {
    private static final CompositeTrigger TRIGGERS = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModAbilityEffects.ETERNAL_HEART_PUNISH,
                              context -> context.getEntity().hasEffect(ModEffects.ETERNAL_HEART.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModAbilityEffects.HOLY_SHIELD_ACTIVE,
                              context -> context.getEntity().hasEffect(ModEffects.HOLY_SHIELD.get()))
    ));

    @Override
    public CompositeTrigger getTriggers() {
        return TRIGGERS;
    }

    @Override
    public double getPriority() {
        return TriggerModulePriority.HIGHEST.priority;
    }
}
