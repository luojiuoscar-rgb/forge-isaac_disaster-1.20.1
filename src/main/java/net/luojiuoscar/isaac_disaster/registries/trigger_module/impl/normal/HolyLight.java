package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public final class HolyLight extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ATTACK_CONTEXT_PREPARE, ModExecutableEffects.HOLY_LIGHT_ATTACHMENT),
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.HOLY_LIGHT_IMPACT,
                    context -> context.getEntity().getRandom().nextDouble() < chance(context.getEntity()))
    ));

    public HolyLight() {
        super(TRIGGER);
    }

    public static double chance(double luck) {
        return 1.0 / Math.max(2, 10 - Math.floor(luck * 0.9));
    }

    public static double chance(LivingEntity entity) {
        return chance(getLuck(entity));
    }
}
