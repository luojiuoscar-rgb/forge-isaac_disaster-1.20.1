package net.luojiuoscar.isaac_disaster.registries.periodic_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModule;
import net.luojiuoscar.isaac_disaster.registries.periodic_module.PeriodicModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;


public final class DeadTooth extends PeriodicModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.DEAD_TOOTH)));

    public DeadTooth() { super(TRIGGER); }

    @Override
    public int getTickInterval(LivingEntity entity, int amplifier, PeriodicModuleQueue queue) { return 4; }

}
