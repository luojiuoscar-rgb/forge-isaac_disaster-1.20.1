package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.Apple.chance;

public final class AppleAttachment extends AttackPreparationEffect {
    @Override
    public double getPriority() {
        return ExecutableEffectPriority.LOWEST.priority;
    }


    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        if (attackContext.isLaserAttack()) {
            attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE,
                    ModExecutableEffects.APPLE_DAMAGE, hit -> hit.getEntity().getRandom().nextDouble() < chance(hit.getEntity())));
        } else if (context.getEntity().getRandom().nextDouble() < chance(context.getEntity())) {
            attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.APPLE_DAMAGE));
        }
    }
}
