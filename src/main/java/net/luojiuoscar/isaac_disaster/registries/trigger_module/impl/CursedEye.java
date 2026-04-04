package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CursedEye implements ITriggerModule {
    private static final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT_NEGATIVE, ModAbilityEffects.CURSED_EYE,
                    context -> {
                        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
                        return !PlayerHelper.hasItem(ItemId.BLACK_CANDLE.getId(), player);
                    })
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }
}
