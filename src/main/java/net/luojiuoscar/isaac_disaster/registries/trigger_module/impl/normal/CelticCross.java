package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import java.util.List;

public final class CelticCross extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.CELTIC_CROSS, context ->
                    context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event
                            && !event.isCanceled() && event.getAmount() > 0
                            && context.getEntity().getRandom().nextDouble() < chance(context.getEntity()))
    ));

    public CelticCross() { super(TRIGGER); }

    static double chance(double luck) { return Math.max(0, Math.min(1, 0.2 + luck * (0.8 / 27.0))); }
    public static double chance(LivingEntity entity) { return chance(getLuck(entity)); }
}
