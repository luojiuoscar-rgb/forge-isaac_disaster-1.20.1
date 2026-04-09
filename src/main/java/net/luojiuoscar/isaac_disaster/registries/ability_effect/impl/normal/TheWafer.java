package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class TheWafer implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event){
            event.setAmount(event.getAmount() * 0.5f);
        }
        return true;
    }
}
