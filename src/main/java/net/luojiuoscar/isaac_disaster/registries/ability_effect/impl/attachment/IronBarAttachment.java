package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;

import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.IronBar.getTriggerChance;

public final class IronBarAttachment extends EffectAttachmentPreparationEffect {
    @Override
    protected void prepare(ExecutableEffectContext context, EffectAttachmentNode node) {
        if (context.getEntity().getRandom().nextDouble() >= getTriggerChance(context.getEntity())) return;
        node.addStatusEffect(EffectAttachmentNode.HitPhase.BEFORE_DAMAGE, ModExecutableEffects.IRON_BAR);
    }
}
