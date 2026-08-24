package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;

/** Attaches The Parasite's split behavior to each prepared attack context. */
public final class Parasite extends TriggerModule {
    public Parasite() {
        super(CompositeTrigger.EMPTY);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.getSplitSequence().add(ModSplitModules.PARASITE.getId(), 1);
    }
}
