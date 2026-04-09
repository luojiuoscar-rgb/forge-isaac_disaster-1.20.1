package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Ipecac implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        if (entity.invulnerableTime > 0) return true;

        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        nums = new ArrayList<>(nums);
        float damage = nums.get(0) < 0 ? 0f : nums.get(0).floatValue();

        explode(entity, context.get(ContextKeys.TARGET_POSITION), damage);
        return true;
    }

    private void explode(LivingEntity attacker, Vec3 pos, float damage){
        if (attacker == null) return;

        damage = (float) computeDamageWithCompensation(damage);

        float power = powerFromDamage(damage);

        LevelHelper.explodeCustom(attacker, pos, power, damage, false, false);

        List<LivingEntity> livingEntities = LevelHelper.selectBySquare(attacker.level(), pos.x, pos.y, pos.z,
                power + 2);

        for (LivingEntity entity : livingEntities){

            if(EntityHelper.isFriendly(entity, attacker)) continue;

            MobEffectInstance poisonEffect = new MobEffectInstance(
                    ModEffects.POISON.get(),
                    (int) Math.min(320, damage * 10),
                    (int) Math.max(0, power - 3),
                    false,
                    true,
                    true
            );

            entity.addEffect(poisonEffect, attacker);
        }
    }

    private static int powerFromDamage(float damage) {
        if (damage <= 10) return 2;
        if (damage <= 25) return 3;
        if (damage <= 50) return 4;
        if (damage <= 100) return 5;
        return 6;
    }

    private static double computeDamageWithCompensation(double damage) {
        double BASE = 4.0;   // damage=0 → 4
        double MAX = 23.0;   // 最终趋近值
        double k = 0.26;     // 衰减速度

        double finalDamage = MAX - (MAX - BASE) * Math.exp(-k * damage);
        double compensation = finalDamage - damage;

        if (compensation < 0.1) compensation = 0;

        return damage + compensation;
    }
}
