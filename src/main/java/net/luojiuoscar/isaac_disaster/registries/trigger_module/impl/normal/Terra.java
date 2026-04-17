package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;

import java.util.List;

public class Terra extends TriggerModule {
    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.BREAK_BLOCK_AND_DROP)
    );

    public Terra() {
        super(CompositeTrigger.EMPTY);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, List<AttackContext> attCtxs) {
        for (var ctx : attCtxs) {
            ctx.getTrigger().addAll(bullet_triggers);
        }
    }
}
