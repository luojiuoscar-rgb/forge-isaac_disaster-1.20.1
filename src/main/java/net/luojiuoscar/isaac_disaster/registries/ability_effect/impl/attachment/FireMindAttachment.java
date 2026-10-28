package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;

import java.util.List;

public final class FireMindAttachment extends EffectAttachmentPreparationEffect {
    private static final List<SimpleTrigger> TRIGGERS = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, ModExecutableEffects.FIRE_MIND_IMPACT),
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.FIRE_MIND_IMPACT)
    );

    @Override
    public double getPriority() { return ExecutableEffectPriority.LOWEST.priority; }

    @Override
    protected void prepare(ExecutableEffectContext context, EffectAttachmentNode node) {
        if (node instanceof AttackContext attack) {
            attack.addSimpleTriggers(TRIGGERS);
            if (attack.isLaserAttack()) return;
        } else {
            node.addStatusEffect(EffectAttachmentNode.HitPhase.AFTER_DAMAGE, ModExecutableEffects.FIRE_MIND_BURN);
        }
        node.offerColor(ModBulletColors.FIRE_MIND.getId());
    }
}
