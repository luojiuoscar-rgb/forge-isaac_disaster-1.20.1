package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TheHighPriestess implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();

        List<LivingEntity> entities = LevelHelper.selectBySphere(entity.level(),
                pos.x, pos.y, pos.z, StatManager.getNearbyRange() * 0.6); // 固定radius

        double highestHealth = entity.getHealth();
        LivingEntity target = entity;

        // 对周围生命最高的生物造成巨量伤害
        for (LivingEntity e : entities){
            double health = e.getHealth();
            if (health > highestHealth) {
                highestHealth = health;
                target = e;
            }
        }

        target.hurt(entity.damageSources().generic(), (float) StatManager.DAMAGE.getBonus() * 100f * amplifier);

        return true;
    }
}
