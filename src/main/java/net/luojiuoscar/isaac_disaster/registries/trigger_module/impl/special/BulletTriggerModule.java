package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTriggerView;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;

public class BulletTriggerModule extends TriggerModule {
    public BulletTriggerModule() {
        super(CompositeTrigger.EMPTY);
    }

    @Override
    public CompositeTriggerView getTriggers(ExecutableEffectContext context) {
        IBulletObject bullet = context.get(ContextKeys.BULLET);
        return bullet == null ? CompositeTrigger.EMPTY.getView() : bullet.getTriggers().getView();
    }
}
