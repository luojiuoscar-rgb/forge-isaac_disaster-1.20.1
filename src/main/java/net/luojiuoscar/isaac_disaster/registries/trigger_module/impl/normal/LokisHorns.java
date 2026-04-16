package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class LokisHorns extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.GET_ATTACK_CONTEXT, ModExecutableEffects.LOKIS_HORNS, context ->
                    context.getEntity().getRandom().nextDouble() < getTriggerChance(context.getEntity()))
    ));

    public LokisHorns() {
        super(TRIGGER);
    }
    public static double getTriggerChance(LivingEntity entity){
        return Math.min(1, (TriggerModule.getLuck(entity) * 0.05 + 0.25));
    }
}
