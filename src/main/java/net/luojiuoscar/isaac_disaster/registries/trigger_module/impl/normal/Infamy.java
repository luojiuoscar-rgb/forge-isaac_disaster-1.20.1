package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public final class Infamy extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.CONE_HEAD,
                    context -> context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event
                            && !event.isCanceled() && event.getAmount() > 0
                            && (event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                                || event.getSource().getDirectEntity() instanceof Projectile
                                || event.getSource().is(ModDamageType.TEAR)
                                || event.getSource().is(ModDamageType.LASER))
                            && event.getSource().getEntity() != context.getEntity()
                            && context.getEntity().getRandom().nextDouble() < 0.5)));

    public Infamy() { super(TRIGGER); }

}
