package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CurseOfTheTower implements ITriggerModule {
    private static final CompositeTrigger triggers = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModAbilityEffects.CURSE_OF_THE_TOWER,
                    context -> {
                        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
                        return !PlayerHelper.hasItem(ItemId.BLACK_CANDLE.getId(), player);
                    })
    ));

    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }
}
