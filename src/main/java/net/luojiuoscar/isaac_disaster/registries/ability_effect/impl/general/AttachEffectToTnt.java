package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;

public class AttachEffectToTnt implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!context.has(ContextKeys.ENTITY)
                || context.get(ContextKeys.ENTITY).isEmpty()
                || !(context.get(ContextKeys.ENTITY).get(0) instanceof IsaacBomb bomb)) return false;

        IExecutableEffect effect = context.get(ContextKeys.EXECUTABLE_EFFECT);
        if (effect == null) return false;

        bomb.getCachedEffect().add(effect);
        return true;
    }
}
