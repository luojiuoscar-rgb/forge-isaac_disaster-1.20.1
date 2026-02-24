package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class Venus implements IRecursiveModule {
    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        List<LivingEntity> entities = LevelHelper.selectBySquare(entity.level(),
                entity.getX(), entity.getY(), entity.getZ(), StatManager.getNearbyRange() * 0.75);

        for (LivingEntity e : entities){
            if (EntityHelper.isFriendly(e, entity)) continue;

            e.addEffect(new MobEffectInstance(
                    ModEffects.CHARM.get(),
                    Mth.clamp(stacks * 60 + 120, 120, 240),
                    0
            ));
        }
    }
}
