package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModulePriority;
import net.minecraft.world.entity.LivingEntity;

public final class Apple extends TriggerModule {
    private static final CompositeTrigger TRIGGER = CompositeTrigger.EMPTY;

    public Apple() { super(TRIGGER); }

    @Override
    public double getPriority() {
        return TriggerModulePriority.LOWEST.priority;
    }

    public static double chance(double luck) { return 1.0 / Math.max(1, 15 - Math.floor(luck)); }
    public static double chance(LivingEntity entity) { return chance(getLuck(entity)); }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        if (attackContext.isLaserAttack()) {
            attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE,
                    ModExecutableEffects.APPLE_DAMAGE, hit -> hit.getEntity().getRandom().nextDouble() < chance(hit.getEntity())));
        } else if (context.getEntity().getRandom().nextDouble() < chance(context.getEntity())) {
            attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.APPLE_DAMAGE));
        }
    }
}
