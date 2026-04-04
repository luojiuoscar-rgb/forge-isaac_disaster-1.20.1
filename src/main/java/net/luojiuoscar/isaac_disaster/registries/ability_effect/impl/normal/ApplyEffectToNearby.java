package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ApplyEffectToNearby implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        IAbilityEffect effect = context.get(ContextKeys.ABILITY_EFFECT);
        // 防止循环叠加
        if (effect == null || effect == ModAbilityEffects.APPLY_EFFECT_TO_NEARBY.get()) return false;

        LivingEntity entity = context.getEntity();
        boolean ignoreFriendly = context.getOrDefault(ContextKeys.BOOLEAN, List.of(true)).get(0);

        // 获取range倍率
        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of(1.));
        if (nums.isEmpty()) nums.add(1.);
        double range = nums.get(0);
        range = Mth.clamp(range, 0.1, 2.0);

        // 施加效果
        Vec3 position = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        List<LivingEntity> entities = LevelHelper.selectBySphere(entity.level(),
                position.x, position.y, position.z, StatManager.getNearbyRange() * range);

        for (LivingEntity e : entities){
            if (ignoreFriendly && EntityHelper.isFriendly(entity, e)) continue;

            effect.apply(context.copy(e)); // 施加效果到对应实体上
        }
        return true;
    }
}