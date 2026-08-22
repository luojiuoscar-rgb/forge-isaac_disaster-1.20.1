package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.special;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModulePriority;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class LowPriorityPlayerPermanentModule extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ATTACK_PLAN, ModExecutableEffects.THE_WIZ_ATTACK_PLAN,
                    context -> context.getEntity() instanceof ServerPlayer player
                            && player.hasEffect(ModEffects.THE_WIZ.get())
                            && context.get(ContextKeys.EVENT) instanceof AttackPlanEvent event
                            && event.getOrigin() == AttackOrigin.PLAYER_PRIMARY)
    ));

    public LowPriorityPlayerPermanentModule() {
        super(TRIGGER);
    }

    @Override
    public double getPriority() {
        return TriggerModulePriority.LOW.priority;
    }
}
