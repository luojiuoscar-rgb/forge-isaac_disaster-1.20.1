package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.minecraftforge.eventbus.api.Event;

public class BeforeTriggerModuleActiveEvent extends Event {
    private final AbilityEffectContext context;

    public BeforeTriggerModuleActiveEvent(AbilityEffectContext context){
        this.context = context;
    }

    public AbilityEffectContext getContext() {
        return context;
    }
}
