package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;

public final class ScorpioAttachment extends EffectAttachmentPreparationEffect {
    @Override
    protected void prepare(ExecutableEffectContext context, EffectAttachmentNode node) {
        node.offerColor(ModBulletColors.POISON.getId());
        node.addStatusEffect(EffectAttachmentNode.HitPhase.BEFORE_DAMAGE, ModExecutableEffects.SCORPIO);
    }
}
