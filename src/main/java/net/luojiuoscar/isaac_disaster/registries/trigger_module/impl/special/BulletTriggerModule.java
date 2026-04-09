package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;

import java.util.List;

public class BulletTriggerModule implements ITriggerModule {
    @Override
    public CompositeTrigger getTriggers() {
        return CompositeTrigger.EMPTY;
    }

    @Override
    public void fire(ExecutableEffectContext context, TriggerType type) {
        IBulletObject bullet = context.get(ContextKeys.BULLET);
        if (bullet == null) return;

        List<SimpleTrigger> triggers = bullet.getTriggers();

        for (SimpleTrigger trigger : triggers){
            if (trigger.getType().is(type)){
                trigger.fire(context);
            }
        }
    }
}
