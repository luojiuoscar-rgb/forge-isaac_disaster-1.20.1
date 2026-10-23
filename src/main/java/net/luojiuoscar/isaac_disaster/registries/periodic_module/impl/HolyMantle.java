package net.luojiuoscar.isaac_disaster.registries.periodic_module.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModule;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class HolyMantle extends PeriodicModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.STACK_HOLY_SHIELD)
    ));

    public HolyMantle() {
        super(TRIGGER);
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, PeriodicModuleQueue queue) {
        return StatManager.getTimeInterval(3);
    }
}
