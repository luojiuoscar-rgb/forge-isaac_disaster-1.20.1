package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class TheWiz implements IRecursiveModule {
    private static final CompositeTrigger triggers = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.THE_WIZ)
    ));

    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 100;
    }
}
