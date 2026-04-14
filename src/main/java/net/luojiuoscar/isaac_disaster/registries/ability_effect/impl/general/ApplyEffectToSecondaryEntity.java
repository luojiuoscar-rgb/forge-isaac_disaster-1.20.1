package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.minecraft.world.entity.LivingEntity;

public class ApplyEffectToSecondaryEntity implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        // get secondary entity
        var entities = context.get(ContextKeys.SECONDARY_ENTITIES);
        if (entities == null || entities.isEmpty()
                || !(entities.get(0) instanceof LivingEntity entity)) return false;
        IExecutableEffect effect = context.get(ContextKeys.EXECUTABLE_EFFECT);
        if (effect == null) return false;

        ExecutableEffectContext newCtx = context.copy(entity);
        effect.apply(newCtx);

        return true;
    }
}
