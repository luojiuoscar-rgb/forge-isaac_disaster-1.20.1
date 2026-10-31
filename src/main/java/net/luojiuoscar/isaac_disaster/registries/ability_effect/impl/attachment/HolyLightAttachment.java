package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;

import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.HolyLight.chance;

public final class HolyLightAttachment extends AttackPreparationEffect {
    private static final List<SimpleTrigger> TEAR_TRIGGERS = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, ModExecutableEffects.HOLY_LIGHT_IMPACT),
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.HOLY_LIGHT_IMPACT)
    );

    @Override
    public double getPriority() {
        return ExecutableEffectPriority.LOWEST.priority;
    }

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        if (attackContext.isLaserAttack()) {
            attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER,
                    ModExecutableEffects.HOLY_LIGHT_IMPACT,
                    hit -> hit.getEntity().getRandom().nextDouble() < chance(hit.getEntity())));
        } else if (context.getEntity().getRandom().nextDouble() < chance(context.getEntity())) {
            attackContext.addSimpleTriggers(TEAR_TRIGGERS);
            attackContext.offerColor(ModBulletColors.HOLY_LIGHT.getId());
        }
    }
}
