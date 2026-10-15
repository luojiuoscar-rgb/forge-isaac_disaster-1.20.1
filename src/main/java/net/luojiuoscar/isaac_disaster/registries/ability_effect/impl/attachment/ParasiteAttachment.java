package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;

public final class ParasiteAttachment extends AttackPreparationEffect {

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.addSplitModule(ModSplitModules.PARASITE.getId(), 1);
    }
}
