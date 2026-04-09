package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;

import java.util.List;

public class Ipecac implements ITriggerModule {
    private static final CompositeTrigger triggers = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY, ModExecutableEffects.IPECAC)
    ));

    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.IPECAC),
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.IPECAC)
    );

    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context) {
        // 添加simpleTrigger到bullet中
        if (context.get(ContextKeys.EVENT) instanceof GetAttackContextEvent event) {
            List<AttackContext> attCtxs = event.getContexts();
            for (var ctx : attCtxs) {
                ctx.getTriggers().addAll(bullet_triggers);
            }
        }
    }
}
