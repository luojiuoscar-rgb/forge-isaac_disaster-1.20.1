package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModulePriority;

import java.util.List;

public class HighPriorityPlayerPermanentModule extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModExecutableEffects.ETERNAL_HEART_PUNISH,
                              context -> context.getEntity().hasEffect(ModEffects.ETERNAL_HEART.get())),

            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModExecutableEffects.HOLY_SHIELD_ACTIVE,
                              context -> context.getEntity().hasEffect(ModEffects.HOLY_SHIELD.get()))
    ));

    public HighPriorityPlayerPermanentModule() {
        super(TRIGGER);
    }

    @Override
    public double getPriority() {
        return TriggerModulePriority.HIGHEST.priority;
    }
}
