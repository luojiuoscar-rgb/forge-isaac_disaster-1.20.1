package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class FireResistance implements IRecursiveModule {
    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 200;
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 240));
    }
}
