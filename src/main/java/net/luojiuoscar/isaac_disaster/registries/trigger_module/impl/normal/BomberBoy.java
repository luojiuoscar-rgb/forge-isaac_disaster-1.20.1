package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;

import java.util.List;

public class BomberBoy extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.TNT_SPAWNED, ModExecutableEffects.ATTACH_BOMBER_BOY)
    ));

    public BomberBoy() {
        super(TRIGGER);
    }
}
