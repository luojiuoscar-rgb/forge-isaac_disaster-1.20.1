package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import java.util.List;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;

public final class LaserAttachment extends AttackPreparationEffect {
    @Override
    public double getPriority() {
        return ExecutableEffectPriority.LOWEST.priority;
    }

    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.LASER_PLUS_BRIMSTONE),
            new SimpleTrigger(ModTriggerTypes.BULLET_TICK, ModExecutableEffects.LASER_PLUS_FETUS),
            new SimpleTrigger(ModTriggerTypes.BULLET_TICK, ModExecutableEffects.LASER_PLUS_MOMS_KNIFE)
    );

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.addSimpleTriggers(bullet_triggers);
    }
}
