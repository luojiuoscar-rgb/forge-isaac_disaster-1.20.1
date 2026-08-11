package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.TriggerModuleRule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.TriggerModuleRuleContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public class BlackCandleCursedEyeRule extends TriggerModuleRule {
    public BlackCandleCursedEyeRule() {
        super(Set.of(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "cursed_eye")),
                Set.of(ModTriggerTypes.ON_HURT_NEGATIVE));
    }

    @Override
    public boolean allows(TriggerModuleRuleContext context) {
        return context.getEntity() instanceof ServerPlayer player
                && !PlayerHelper.hasItem(ItemId.BLACK_CANDLE.getId(), player);
    }
}
