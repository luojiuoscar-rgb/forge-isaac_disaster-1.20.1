package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class BlindRage implements IAbilityEffect {
    public static int getCooldownTicks(int amplifier) {
        return 25 * Mth.clamp(amplifier, 1, 2);
    }

    @Override
    public double getPriority() {
        return ExecutableEffectPriority.LOWEST.priority;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        int amplifier = Mth.clamp(context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue(), 1, 2);


        LivingEntity entity = context.getEntity();
        entity.invulnerableTime = getCooldownTicks(amplifier);
        return true;
    }
}
