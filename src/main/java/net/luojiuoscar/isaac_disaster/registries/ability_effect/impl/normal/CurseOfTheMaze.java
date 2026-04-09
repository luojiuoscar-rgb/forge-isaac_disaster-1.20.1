package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;

public class CurseOfTheMaze implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();

        if (entity.getRandom().nextDouble() < 0.2){ // 20%
            EntityHelper.teleportToRandomLocation(entity, 0.5 * StatManager.getNearbyRange());
        }
        return true;
    }
}
