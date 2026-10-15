package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;

public final class CricketsBodyAttachment extends AttackPreparationEffect {

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.addSplitModule(ModSplitModules.CRICKETS_BODY.getId(), 1);
    }
}
