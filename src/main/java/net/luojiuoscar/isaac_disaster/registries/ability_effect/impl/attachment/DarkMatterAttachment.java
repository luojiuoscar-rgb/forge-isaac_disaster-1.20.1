package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;

import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.DarkMatter.chance;

public final class DarkMatterAttachment extends EffectAttachmentPreparationEffect {
    @Override
    protected void prepare(ExecutableEffectContext context, EffectAttachmentNode node) {
        if (context.getEntity().getRandom().nextDouble() >= chance(context.getEntity())) return;
        node.offerColor(ModBulletColors.DARK_MATTER.getId());
        node.addStatusEffect(EffectAttachmentNode.HitPhase.BEFORE_DAMAGE, ModExecutableEffects.DARK_MATTER);
    }
}
