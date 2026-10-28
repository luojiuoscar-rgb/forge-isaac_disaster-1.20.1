package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class IronBar extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE, ModExecutableEffects.IRON_BAR_ATTACHMENT),
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.IRON_BAR, context ->
                            context.getEntity().getRandom().nextDouble()
                                    < getTriggerChance(context.getEntity()))
    ));

    public IronBar() {
        super(TRIGGER);
    }

    public static double getTriggerChance(LivingEntity entity){
        return LuckTriggerChance.ironBar(TriggerModule.getLuck(entity));
    }

}
