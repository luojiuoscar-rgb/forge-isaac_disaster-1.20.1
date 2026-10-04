package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;

/** Attaches Cricket's Body's split behavior during attack preparation. */
public final class CricketsBody extends TriggerModule {
    public CricketsBody() {
        super(CompositeTrigger.EMPTY);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.addSplitModule(ModSplitModules.CRICKETS_BODY.getId(), 1);
    }
}
