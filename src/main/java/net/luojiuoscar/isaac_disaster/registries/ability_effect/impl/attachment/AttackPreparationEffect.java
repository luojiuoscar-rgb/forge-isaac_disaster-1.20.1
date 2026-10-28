package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;

/** Transforms an attack during its preparation phase, before projectile construction. */
public abstract class AttackPreparationEffect implements IExecutableEffect {
    @Override
    public final TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE;
    }

    @Override
    public final void apply(ExecutableEffectContext context) {
        AttackContext attackContext = context.get(ContextKeys.ATTACK_CONTEXT);
        if (attackContext != null) prepare(context, attackContext);
    }

    protected abstract void prepare(ExecutableEffectContext context, AttackContext attackContext);
}
