package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class GildedKey implements IRecursiveModule {
    private static final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModAbilityEffects.GILDED_KEY)
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }
}
