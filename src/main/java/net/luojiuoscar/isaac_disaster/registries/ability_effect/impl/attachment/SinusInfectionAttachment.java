package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import java.util.HashSet;
import java.util.List;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;

public final class SinusInfectionAttachment extends AttackPreparationEffect {
    private static final List<SimpleTrigger> BULLET_TRIGGERS = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_STICK_START, ModExecutableEffects.SINUS_INFECTION_POISON),
            new SimpleTrigger(ModTriggerTypes.BULLET_STICK_TICK, ModExecutableEffects.SINUS_INFECTION_DAMAGE));

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        if (!ModAttackTypes.BULLET.getId().equals(attackContext.getTypeId())) return;
        if (context.getEntity().getRandom().nextDouble() >= 0.20D) return;
        attackContext.setMaxStickDurationTicks(200);
        attackContext.setBulletScale(attackContext.getBulletScale() * 1.5D, true);
        attackContext.setColorRl(ModBulletColors.POISON.getId());
        var visuals = new HashSet<>(attackContext.getVisualIds());
        visuals.add(ModBulletVisuals.SINUS_INFECTION_TEAR.getId());
        attackContext.setVisualIds(visuals);
        attackContext.addSimpleTriggers(BULLET_TRIGGERS);
    }
}
