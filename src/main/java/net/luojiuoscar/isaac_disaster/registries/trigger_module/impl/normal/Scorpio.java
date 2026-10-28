package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import java.util.List;

public final class Scorpio extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE, ModExecutableEffects.SCORPIO_ATTACHMENT),
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.SCORPIO)
    ));

    public Scorpio() { super(TRIGGER); }

}
