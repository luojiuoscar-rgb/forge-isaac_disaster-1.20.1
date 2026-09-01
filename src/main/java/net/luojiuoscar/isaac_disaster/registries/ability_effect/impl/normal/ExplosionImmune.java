package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class ExplosionImmune implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.ON_HURT;
    }

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
