package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;

import java.util.List;

public class Ipecac extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.IPECAC)
    ));

    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.IPECAC),
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.IPECAC),
            new SimpleTrigger(ModTriggerTypes.BULLET_END_OF_LIFE, ModExecutableEffects.IPECAC)
    );

    public Ipecac() {
        super(TRIGGER);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, List<AttackContext> attCtxs) {
        for (var ctx : attCtxs) {
            ctx.getTrigger().addAll(bullet_triggers);
        }
    }
}
