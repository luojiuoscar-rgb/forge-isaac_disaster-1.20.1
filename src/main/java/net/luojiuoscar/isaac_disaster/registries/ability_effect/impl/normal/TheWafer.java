package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;

public class TheWafer implements IAbilityEffect {
    @Override
    public double getPriority() {
        return ExecutableEffectPriority.LOWEST.priority;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {

        return true;
    }
}
