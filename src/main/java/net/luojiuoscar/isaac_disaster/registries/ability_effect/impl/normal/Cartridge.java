package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class Cartridge implements IAbilityEffect {
    public static double getChance(double amplifier, double luck) {
        if (!Double.isFinite(amplifier) || amplifier <= 0) return 0;
        if (!Double.isFinite(luck)) luck = 0;
        return amplifier / Math.max(amplifier, 20 - luck * 0.5);
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        amplifier = Math.max(amplifier, 1);

        if (entity.getRandom().nextDouble() < getChance(amplifier, getLuck(entity))){
            entity.addEffect(new MobEffectInstance(ModEffects.INVINCIBLE.get(), 120));
        }

        return true;
    }
}
