package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;

import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.MomsEyeshadow.getTriggerChance;

public final class MomsEyeshadowAttachment extends EffectAttachmentPreparationEffect {
    @Override
    protected void prepare(ExecutableEffectContext context, EffectAttachmentNode node) {
        if (context.getEntity().getRandom().nextDouble() >= getTriggerChance(context.getEntity())) return;
        node.offerColor(ModBulletColors.CHARM.getId());
        node.addStatusEffect(EffectAttachmentNode.HitPhase.BEFORE_DAMAGE, ModExecutableEffects.MOMS_EYESHADOW);
    }
}
