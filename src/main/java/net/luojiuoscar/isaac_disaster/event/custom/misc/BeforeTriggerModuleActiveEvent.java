package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.minecraftforge.eventbus.api.Event;

public class BeforeTriggerModuleActiveEvent extends Event {
    private final ExecutableEffectContext context;

    public BeforeTriggerModuleActiveEvent(ExecutableEffectContext context){
        this.context = context;
    }

    public ExecutableEffectContext getContext() {
        return context;
    }
}
