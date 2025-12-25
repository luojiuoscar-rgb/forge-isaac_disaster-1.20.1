package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.*;

import java.util.Set;

public class Laser implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.GET_ATTACK_CONTEXT,
                TriggerCategory.BULLET_HIT_ENTITY_BEFORE
        );
    }

    @Override
    public void getAttackContext(GetAttackContextEvent event, int stacks, TriggerModuleQueue queue) {
        event.getContext().addTriggerModule(ModTriggerModule.LASER.getId(), 1);
    }

    @Override
    public void beforeBulletHitEntity(IsaacAttackBeforeHitEntityEvent event, int stacks, TriggerModuleQueue queue) {
        if (event.getAttackType().equals(ModAttackType.BRIMSTONE.getId())){
            event.setDamage(event.getDamage() * 1.5);
        }
    }

    @Override
    public double getPriority(){
        return TriggerModulePriority.LOWEST.priority;
    }
}
