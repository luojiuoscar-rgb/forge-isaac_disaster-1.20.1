package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class ThrowBomb implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();
        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (nums.size() < 2) {
            nums = List.of(80., 4.);
        }

        EntityHelper.throwBomb(entity, nums.get(0).intValue(), nums.get(1).intValue());
    }
}