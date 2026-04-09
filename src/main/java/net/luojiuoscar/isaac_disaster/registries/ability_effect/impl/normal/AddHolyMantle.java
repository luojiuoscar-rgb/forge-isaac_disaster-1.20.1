package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.IStackableEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;

public class AddHolyMantle implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        ((IStackableEffect) ModEffects.HOLY_SHIELD.get()).stack(context.getEntity(), 1);
        return true;
    }
}
