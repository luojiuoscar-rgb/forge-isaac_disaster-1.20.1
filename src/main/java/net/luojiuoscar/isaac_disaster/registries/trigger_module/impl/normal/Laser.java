package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModulePriority;

import java.util.List;

public class Laser extends TriggerModule {
    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.LASER_PLUS_BRIMSTONE),
            new SimpleTrigger(ModTriggerTypes.BULLET_TICK, ModExecutableEffects.LASER_PLUS_FETUS)
    );

    public Laser() {
        super(CompositeTrigger.EMPTY);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context) {
        // 添加simpleTrigger到bullet中
        if (context.get(ContextKeys.EVENT) instanceof GetAttackContextEvent event) {
            List<AttackContext> attCtxs = event.getContexts();
            for (var ctx : attCtxs) {
                ctx.getTrigger().addAll(bullet_triggers);
            }
        }
    }

    @Override
    public double getPriority(){
        return TriggerModulePriority.LOWEST.priority;
    }
}
