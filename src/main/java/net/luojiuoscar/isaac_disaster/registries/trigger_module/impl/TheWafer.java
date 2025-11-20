package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Set;

public class TheWafer implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(TriggerCategory.ON_HURT);
    }

    @Override
    public void onHurt(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {
        event.setAmount(event.getAmount() * 0.5f);
    }

    @Override
    public double getPriority(){
        return -1;
    }
}
