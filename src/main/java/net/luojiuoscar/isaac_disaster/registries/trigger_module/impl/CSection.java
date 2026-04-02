package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CSection implements ITriggerModule {
    private final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BEFORE_PERFORM_ATTACK, ModAbilityEffects.BRIMSTONE_PLUS_C_SECTION,
                    context -> {
                        if (!(context.get(ContextKeys.EVENT) instanceof BeforePerformAttackEvent event)) return false;
                        if (!(context.getEntity() instanceof ServerPlayer)) return false;
                        return event.getAttackType() == ModAttackType.BRIMSTONE.get();
                    })
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }
}
