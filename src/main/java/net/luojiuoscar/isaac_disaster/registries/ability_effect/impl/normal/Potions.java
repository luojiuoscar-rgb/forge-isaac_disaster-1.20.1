package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Potions implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        var potions = context.get(ContextKeys.POTIONS);
        if (potions.isEmpty()) return false;

        int multiplier = context.getOrDefault(ContextKeys.AMPLIFIER, 1);
        LivingEntity entity = context.getEntity();

        for (var potion : potions){
            entity.addEffect(new MobEffectInstance(
                    potion.effect,
                    potion.duration + potion.duration_increment * multiplier,
                    potion.duration + potion.amplifier_increment * multiplier,
                    false,
                    potion.has_particle,
                    potion.has_particle
            ));
        }
        return true;
    }
}