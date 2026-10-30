package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.system.combat_effect.attachment.EffectAttachmentNode;

import java.util.HashSet;

public final class UranusAttachment extends EffectAttachmentPreparationEffect {
    @Override
    public double getPriority() { return ExecutableEffectPriority.LOWEST.priority; }

    @Override
    protected void prepare(ExecutableEffectContext context, EffectAttachmentNode node) {
        node.offerColor(ModBulletColors.URANUS.getId());
        node.addStatusEffect(EffectAttachmentNode.HitPhase.AFTER_DAMAGE, ModExecutableEffects.URANUS_SLOW);
        if (node instanceof AttackContext attack) attachProjectile(attack);
    }

    /** Also used for clean ice shards, which deliberately skip player attachment preparation. */
    public static void attachProjectile(AttackContext attack) {
        attack.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.KILL_ENTITY, ModExecutableEffects.URANUS_FREEZE_ON_KILL));
        if (!attack.isLaserAttack()) {
            var visuals = new HashSet<>(attack.getVisualIds());
            visuals.add(ModBulletVisuals.URANUS_ICE_TEAR.getId());
            attack.setVisualIds(visuals);
        }
    }
}
