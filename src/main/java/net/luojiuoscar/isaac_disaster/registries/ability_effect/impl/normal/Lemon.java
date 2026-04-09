package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.custom.LemonEffectCloud;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Lemon implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        nums = new ArrayList<>(nums);
        if (nums.size() < 5) {
            nums = List.of(0.4, 100., 0., 10., 2.5);
        }

        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        float range = Mth.clamp(nums.get(0).floatValue(), 0.1f, 2f);
        int duration = Mth.clamp(nums.get(1).intValue(), 20, 1200);
        int radius_per_tick = nums.get(2).intValue();
        int waitTime = Mth.clamp(nums.get(3).intValue(), 20, 1200);
        float damage = nums.get(4).floatValue() < 0 ? 0 : nums.get(4).floatValue();


        // 创建药水云
        LemonEffectCloud cloud = new LemonEffectCloud(entity.level(), pos.x, pos.y, pos.z,
                entity, (float) StatManager.getNearbyRange() * range,
                duration, radius_per_tick, waitTime,
                (float) StatManager.DAMAGE.getBonus() * damage);

        // 生成实体
        entity.level().addFreshEntity(cloud);
        return true;
    }
}