package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.event.custom.misc.RightClickTickEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;

import java.util.Set;

public class Technology2 implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.RIGHT_CLICK_TICK
        );
    }

    @Override
    public void onRightClickTick(RightClickTickEvent event, int stacks, TriggerModuleQueue queue) {
        AttackType attack = ModAttackType.TECHNOLOGY2.get();
        attack.performAttack(attack.getAttackContexts(event.getPlayer(), 1));
    }
}
