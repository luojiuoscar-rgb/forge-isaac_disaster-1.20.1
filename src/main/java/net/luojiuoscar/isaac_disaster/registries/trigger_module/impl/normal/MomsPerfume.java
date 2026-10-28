package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class MomsPerfume extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE, ModExecutableEffects.MOMS_PERFUME_ATTACHMENT),
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.MOMS_PERFUME, context ->
                    context.getEntity().getRandom().nextDouble() < getTriggerChance(context.getEntity()))
    ));

    public MomsPerfume() {
        super(TRIGGER);
    }

    static double getTriggerChance(double luck) {
        double flooredLuck = Math.floor(luck);
        if (flooredLuck >= 85.0) return 1.0;
        return Math.max(0.0, Math.min(1.0, 15.0 / (100.0 - flooredLuck)));
    }

    public static double getTriggerChance(LivingEntity entity) {
        return getTriggerChance(TriggerModule.getLuck(entity));
    }

}
