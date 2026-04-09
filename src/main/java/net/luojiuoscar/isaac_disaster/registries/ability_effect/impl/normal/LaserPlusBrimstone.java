package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;

public class LaserPlusBrimstone implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (context.get(ContextKeys.EVENT) instanceof IsaacAttackBeforeHitEntityEvent event){
            if (event.getAttackType().equals(ModAttackType.BRIMSTONE.getId())){
                event.setDamage(event.getDamage() * 1.5);
                return true;
            }
        }
        return false;
    }
}
