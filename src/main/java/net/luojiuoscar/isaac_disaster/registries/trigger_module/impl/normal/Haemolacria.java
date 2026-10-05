package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;

public final class Haemolacria extends TriggerModule {
    private static final CompositeTrigger TRIGGER = CompositeTrigger.EMPTY;

    public Haemolacria() {
        super(TRIGGER);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        if (!ModAttackTypes.BULLET.getId().equals(attackContext.getRootTypeId())) return;
        attackContext.addTrajectoryModule(ModTrajectoryModules.GRAVITY.getId(), 1);
        attackContext.addSplitModule(ModSplitModules.HAEMOLACRIA.getId(), 1);
        attackContext.setBulletScale(attackContext.getBulletScale() * 1.71, true);
    }
}
