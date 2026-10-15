package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModulePriority;

public final class AppleDamage implements IAbilityEffect {
    @Override
    public double getPriority() {
        return TriggerModulePriority.LOWEST.priority;
    }

    @Override
    public TriggerType getRequiredTriggerType() { return ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE; }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof IsaacAttackBeforeHitEntityEvent event)) return false;
        event.setDamage(event.getDamage() * 4);
        return true;
    }
}
