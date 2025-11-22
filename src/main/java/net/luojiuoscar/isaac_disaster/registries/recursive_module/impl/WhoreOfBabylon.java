package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class WhoreOfBabylon implements IRecursiveModule {
    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 10;
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        if (entity.getHealth() > entity.getMaxHealth() * 0.1) return;
        MobEffectInstance instance = entity.getEffect(ModEffects.BABYLON.get());
        if (instance != null){
            double duration = instance.getDuration();
            if (duration > 60){
                return; // 小于3秒的时候再添加新的效果
            }
        }

        entity.addEffect(new MobEffectInstance(ModEffects.BABYLON.get(), 240, 0));
    }
}
