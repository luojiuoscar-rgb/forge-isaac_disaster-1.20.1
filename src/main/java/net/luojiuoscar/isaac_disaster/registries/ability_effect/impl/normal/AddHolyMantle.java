package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.custom.HolyShieldEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;

public class AddHolyMantle implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        HolyShieldEffect.stack(context.getEntity(), 1);
        return true;
    }
}
