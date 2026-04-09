package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.BeforePerformAttackEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class CSection extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.BEFORE_PERFORM_ATTACK, ModExecutableEffects.BRIMSTONE_PLUS_C_SECTION,
                    context -> {
                        if (!(context.get(ContextKeys.EVENT) instanceof BeforePerformAttackEvent event)) return false;
                        if (!(context.getEntity() instanceof ServerPlayer)) return false;
                        return event.getAttackType() == ModAttackType.BRIMSTONE.get();
                    })
    ));

    public CSection() {
        super(TRIGGER);
    }
}
