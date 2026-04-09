package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ExplosionImmune implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event){
            DamageSource source = event.getSource();

            if (source.getMsgId().contains("explosion")){
                event.setCanceled(true);
            }
            return true;
        }
        return false;
    }
}
