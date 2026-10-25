package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.system.explosion.ExplosionParameters;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class Kamikaze implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();

        if (amplifier > 1){
            explode(entity, 7, 80);
        }else {
            explode(entity, 4, 35);
        }
        return true;
    }

    private void explode(LivingEntity entity, float power, float damage){
        if (entity.level() instanceof ServerLevel level) {
            LevelHelper.explodeCustom(level, entity.position(), level.damageSources().explosion(entity, entity),
                    ExplosionParameters.standard(power, damage).withIgnoreFriendly(true).withCausesFire(false));
        }
        entity.hurt(PlayerHelper.getSelfDamageSource(entity), (float) StatManager.MAX_HEALTH.getBonus());
    }
}
