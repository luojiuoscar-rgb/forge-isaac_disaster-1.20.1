package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class MomsPerfume extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
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

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        LivingEntity entity = context.getEntity();
        List<SimpleTrigger> bulletTriggers = List.of(
                new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.MOMS_PERFUME)
        );
        if (entity.getRandom().nextDouble() < getTriggerChance(entity)) {
            attackContext.colorRl = ModBulletColor.FEAR.getId();
            attackContext.getTrigger().addAll(bulletTriggers);
        }
    }
}
