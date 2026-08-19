package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Potions implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var potions = context.get(ContextKeys.POTIONS);
        if (potions == null || potions.isEmpty()) return false;

        int multiplier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue() - 1;
        LivingEntity entity = context.getEntity();

        for (var potion : potions){
            int finalDuration = potion.resolveDuration(multiplier);
            int finalAmplifier = potion.resolveAmplifier(multiplier);

            entity.addEffect(new MobEffectInstance(
                    potion.effect,
                    finalDuration,
                    finalAmplifier,
                    false,
                    potion.has_particle,
                    potion.has_particle
            ));
        }
        return true;
    }
}
