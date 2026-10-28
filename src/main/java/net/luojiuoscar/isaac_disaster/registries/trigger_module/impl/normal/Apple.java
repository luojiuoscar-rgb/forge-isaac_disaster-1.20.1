package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import java.util.List;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

public final class Apple extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE, ModExecutableEffects.APPLE_ATTACHMENT)
    ));

    public Apple() { super(TRIGGER); }

    public static double chance(double luck) { return 1.0 / Math.max(1, 15 - Math.floor(luck)); }
    public static double chance(LivingEntity entity) { return chance(getLuck(entity)); }

}
