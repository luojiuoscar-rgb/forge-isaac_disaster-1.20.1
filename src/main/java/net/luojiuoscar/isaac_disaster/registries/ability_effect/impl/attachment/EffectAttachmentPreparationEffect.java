package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;

/** Runs an attachment once for each newly created effect node. */
public abstract class EffectAttachmentPreparationEffect implements IExecutableEffect {
    @Override
    public final TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE;
    }

    @Override
    public final void apply(ExecutableEffectContext context) {
        var node = context.get(ContextKeys.EFFECT_ATTACHMENT_NODE);
        if (node != null) prepare(context, node);
    }

    protected abstract void prepare(ExecutableEffectContext context, EffectAttachmentNode node);
}
