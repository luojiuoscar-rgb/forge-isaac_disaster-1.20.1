package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;

public class BulletTriggerModule extends TriggerModule {
    public BulletTriggerModule() {
        super(CompositeTrigger.EMPTY);
    }

    @Override
    public void fire(ExecutableEffectContext context, TriggerType type) {
        IBulletObject bullet = context.get(ContextKeys.BULLET);
        if (bullet == null) return;

        bullet.getTriggers().fire(context, type);
    }
}
