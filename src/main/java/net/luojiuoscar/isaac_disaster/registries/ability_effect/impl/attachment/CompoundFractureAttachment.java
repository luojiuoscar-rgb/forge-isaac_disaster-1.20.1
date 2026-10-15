package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.server.level.ServerPlayer;

public final class CompoundFractureAttachment extends AttackPreparationEffect {

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        if (context.getEntity() instanceof ServerPlayer player
                && PlayerHelper.hasItem(ModPassiveItems.CRICKETS_BODY.getId(), player)) {
            attackContext.addSplitModule(ModSplitModules.COMPOUND_FRACTURE_ENHANCED.getId(), 1);
            return;
        }
        attackContext.addSplitModule(ModSplitModules.COMPOUND_FRACTURE.getId(), 1);
    }
}
