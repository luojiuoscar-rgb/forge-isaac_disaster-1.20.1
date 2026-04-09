package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ExplosionRegeneration implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event){
            DamageSource source = event.getSource();

            if (source.getMsgId().contains("explosion")){
                context.set(ContextKeys.AMPLIFIER, 0.4);
                ModExecutableEffects.HEAL.get().apply(context);
            }
            return true;
        }
        return false;
    }
}
