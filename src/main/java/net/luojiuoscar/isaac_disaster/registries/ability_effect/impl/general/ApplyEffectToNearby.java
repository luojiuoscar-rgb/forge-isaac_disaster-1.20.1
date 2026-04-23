package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ApplyEffectToNearby implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        IExecutableEffect effect = context.get(ContextKeys.EXECUTABLE_EFFECT);
        // 防止循环叠加
        if (effect == null || effect == ModExecutableEffects.APPLY_EFFECT_TO_NEARBY.get()) return false;

        LivingEntity entity = context.getEntity();
        boolean ignoreFriendly = context.getOrDefault(ContextKeys.BOOLEAN, List.of(true)).get(0);

        // 获取range倍率 默认1
        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        nums = new ArrayList<>(nums);
        if (nums.isEmpty()) nums.add(1.);
        double range = nums.get(0);
        range = Mth.clamp(range, 0.1, 2.0);

        // 施加效果
        Vec3 position = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        List<LivingEntity> entities = LevelHelper.selectBySphere(entity.level(),
                position, StatManager.getNearbyRange() * range);

        for (LivingEntity e : entities){            effect.apply(context.copy(e)); // 施加效果到对应实体上

            if (ignoreFriendly && EntityHelper.isFriendly(entity, e)) continue;

        }
        return true;
    }
}