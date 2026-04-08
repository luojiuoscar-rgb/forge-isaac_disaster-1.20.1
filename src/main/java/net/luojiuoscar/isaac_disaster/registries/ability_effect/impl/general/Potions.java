package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Potions implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        var potions = context.get(ContextKeys.POTIONS);
        if (potions.isEmpty()) return false;

        int multiplier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        LivingEntity entity = context.getEntity();

        for (var potion : potions){
            int  finalDuration= Mth.clamp(potion.duration + potion.duration_increment * multiplier, 0, 1000000);
            int finalAmplifier = Mth.clamp(potion.amplifier + potion.amplifier_increment * multiplier, 0, 255);

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