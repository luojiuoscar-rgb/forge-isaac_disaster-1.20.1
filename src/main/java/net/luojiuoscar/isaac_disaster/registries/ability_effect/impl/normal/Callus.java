package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class Callus implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event)) return false;
        int amount = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();

        event.setAmount(event.getAmount() - amount);
        return true;
    }
}
