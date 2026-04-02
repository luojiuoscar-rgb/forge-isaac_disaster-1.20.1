package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class BlindRage implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        int amplifier = Mth.clamp(context.getOrDefault(ContextKeys.AMPLIFIER, 1), 1, 2);


        LivingEntity entity = context.getEntity();
        entity.invulnerableTime = Math.min(25 * amplifier, 50);
        return true;
    }
}
