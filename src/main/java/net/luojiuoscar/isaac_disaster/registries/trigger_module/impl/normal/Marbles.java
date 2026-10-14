package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import java.util.List;

public final class Marbles extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.MARBLES, context ->
                    context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event
                            && !event.isCanceled() && event.getAmount() > 0
                            && context.getEntity().getRandom().nextDouble() < chance(context.getEntity()))
    ));

    public Marbles() { super(TRIGGER); }

    static double chance(double luck) { return 0.1; }
    public static double chance(LivingEntity entity) { return chance(getLuck(entity)); }
}
