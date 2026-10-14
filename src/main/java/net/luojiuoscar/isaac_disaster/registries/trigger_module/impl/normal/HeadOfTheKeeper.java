package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;


public final class HeadOfTheKeeper extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger();

    public HeadOfTheKeeper() { super(TRIGGER); }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE,
                ModExecutableEffects.HEAD_OF_THE_KEEPER,
                hit -> hit.getEntity().getRandom().nextDouble() < 0.05));
    }
}
