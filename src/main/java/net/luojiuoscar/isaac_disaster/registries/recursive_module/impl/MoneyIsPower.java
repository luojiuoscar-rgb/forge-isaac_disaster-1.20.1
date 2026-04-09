package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.MoneyIsPower.MONEY_IS_POWER_ADDER;

public class MoneyIsPower implements IRecursiveModule {
    private static final CompositeTrigger triggers = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModExecutableEffects.MONEY_IS_POWER)
    ));

    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }

    @Override
    public void handleRemove(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (instance == null) return;
        instance.removeModifier(MONEY_IS_POWER_ADDER);
    }
}
