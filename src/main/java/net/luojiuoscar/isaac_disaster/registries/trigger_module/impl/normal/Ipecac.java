package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;

import java.util.List;

public class Ipecac extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.IPECAC)
    ));

    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.IPECAC,
                    Ipecac::isAllowedSequence),
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.IPECAC,
                    Ipecac::isAllowedSequence),
            new SimpleTrigger(ModTriggerTypes.BULLET_END_OF_LIFE, ModExecutableEffects.IPECAC,
                    Ipecac::isAllowedSequence)
    );

    public Ipecac() {
        super(TRIGGER);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.addSimpleTriggers(bullet_triggers);
        attackContext.addTrajectoryModule(ModTrajectoryModules.GRAVITY.getId(),
                context.getOrDefault(ContextKeys.AMPLIFIER, 1.0D).intValue());
    }

    private static boolean isAllowedSequence(ExecutableEffectContext context) {
        IBulletObject bullet = context.get(ContextKeys.BULLET);
        return bullet == null || bullet.getAttackSequenceIndex() % 4 == 0;
    }
}
