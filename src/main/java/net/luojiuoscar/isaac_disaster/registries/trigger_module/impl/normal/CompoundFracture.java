package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.server.level.ServerPlayer;

/** Attaches Compound Fracture to prepared attacks. */
public final class CompoundFracture extends TriggerModule {
    private static final CompositeTrigger TRIGGER = CompositeTrigger.EMPTY;

    public CompoundFracture() {
        super(TRIGGER);
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        if (context.getEntity() instanceof ServerPlayer player
                && PlayerHelper.hasItem(ModPassiveItems.CRICKETS_BODY.getId(), player)) {
            attackContext.addSplitModule(ModSplitModules.COMPOUND_FRACTURE_ENHANCED.getId(), 1);
            return;
        }
        attackContext.addSplitModule(ModSplitModules.COMPOUND_FRACTURE.getId(), 1);
    }
}
