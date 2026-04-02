package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;

public class Kamikaze implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1);

        if (amplifier > 1){
            explode(entity, 7, 80);
        }else {
            explode(entity, 4, 35);
        }
        return true;
    }

    private void explode(LivingEntity entity, float power, float damage){
        LevelHelper.explodeCustom(entity, entity.position(), power, damage, true, false);
        entity.hurt(entity.damageSources().genericKill(), (float) StatManager.MAX_HEALTH.getBonus());
    }
}