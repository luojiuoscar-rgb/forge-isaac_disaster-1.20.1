package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class BlackCandle implements IRecursiveModule {
    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 4;
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        List<MobEffectInstance> toRemove = new ArrayList<>();
        // 获取所有需要被移除的效果
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL
                    && !effect.getEffect().getCurativeItems().isEmpty()) {
                toRemove.add(effect);
            }
        }

        // 移除效果
        for (MobEffectInstance effect : toRemove) {
            entity.removeEffect(effect.getEffect());
        }
    }
}
