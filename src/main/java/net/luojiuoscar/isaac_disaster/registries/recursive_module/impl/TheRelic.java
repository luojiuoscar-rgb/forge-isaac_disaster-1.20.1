package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class TheRelic implements IRecursiveModule {
    private static final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModAbilityEffects.GIVE_ITEM)
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }

    @Override
    public void modifyContext(AbilityEffectContext context) {
        context.set(ContextKeys.ITEM, ModItems.SOUL_HEART.get());
    }

    @Override
    public int getInitialTick() {
        return StatManager.getTimeInterval(12);
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return StatManager.getTimeInterval(entity.getRandom().nextInt(12,19));
    }
}
