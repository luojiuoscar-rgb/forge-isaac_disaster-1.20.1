package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class TheSoul implements IRecursiveModule {
    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return entity.getRandom().nextInt(100, 220);
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        entity.addEffect(new MobEffectInstance(
                ModEffects.SOUL_STATE.get(), 120, 0, false, false, true));
    }
}
