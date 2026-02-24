package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraftforge.eventbus.api.Event;

public class BeforeTriggerModuleActiveEvent extends Event {
    private final Event event;
    private final TriggerModuleQueue queue;

    public BeforeTriggerModuleActiveEvent(Event event, TriggerModuleQueue queue){
        this.event = event;
        this.queue = queue;
    }

    public TriggerModuleQueue getQueue() {
        return queue;
    }

    public Event getEvent() {
        return event;
    }
}
