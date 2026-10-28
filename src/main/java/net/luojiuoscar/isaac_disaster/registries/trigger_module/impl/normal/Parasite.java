package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import java.util.List;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;

/** Attaches The Parasite's split behavior to each prepared attack context. */
public final class Parasite extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE, ModExecutableEffects.PARASITE_ATTACHMENT)
    ));
    public Parasite() {
        super(TRIGGER);
    }

}
