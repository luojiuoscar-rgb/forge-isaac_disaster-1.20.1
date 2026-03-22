package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.LemonEffectCloud;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class Lemon implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();
        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (nums.size() < 5) {
            nums = List.of(0.4, 100., 0., 10., 2.5);
        }

        // 创建药水云
        LemonEffectCloud cloud = new LemonEffectCloud(entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                entity, (float) StatManager.getNearbyRange() * nums.get(0).floatValue(),
                nums.get(1).intValue(),
                nums.get(2).intValue(), nums.get(3).intValue(),
                (float) StatManager.DAMAGE.getBonus() * nums.get(4).floatValue());

        // 生成实体
        entity.level().addFreshEntity(cloud);
    }
}