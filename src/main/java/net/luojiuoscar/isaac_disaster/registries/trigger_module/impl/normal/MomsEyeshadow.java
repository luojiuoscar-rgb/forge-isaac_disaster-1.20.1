package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class MomsEyeshadow extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ATTACK_CONTEXT_PREPARE, ModExecutableEffects.MOMS_EYESHADOW_ATTACHMENT),
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.MOMS_EYESHADOW, context ->
                    context.getEntity().getRandom().nextDouble() < getTriggerChance(context.getEntity()))
    ));

    public MomsEyeshadow() {
        super(TRIGGER);
    }

    public static double getTriggerChance(LivingEntity entity){
        return LuckTriggerChance.momsEyeshadow(TriggerModule.getLuck(entity));
    }

}
