package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public final class Virgo extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.VIRGO, context ->
                    context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event
                            && !event.isCanceled() && event.getAmount() > 0
                            && context.getEntity().getRandom().nextDouble() < chance(context.getEntity()))
    ));

    public Virgo() { super(TRIGGER); }

    static double chance(double luck) {
        return 1.0 / Math.max(2, 4 - Math.floor((Double.isFinite(luck) ? luck : 0) * 0.2));
    }

    public static double chance(LivingEntity entity) { return chance(getLuck(entity)); }
}
