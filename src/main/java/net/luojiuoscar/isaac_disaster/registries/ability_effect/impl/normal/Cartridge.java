package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Cartridge implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        amplifier = Math.max(amplifier, 1);

        if (entity.getRandom().nextDouble() < amplifier / Math.max(amplifier, 20 - getLuck(entity) * 0.5)){
            entity.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), 120));
        }

        return true;
    }
}
