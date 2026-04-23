package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class MomsEyeshadow extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.MOMS_EYESHADOW, context ->
                    context.getEntity().getRandom().nextDouble() < getTriggerChance(context.getEntity()))
    ));

    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.MOMS_EYESHADOW)
    );

    public MomsEyeshadow() {
        super(TRIGGER);
    }

    public static double getTriggerChance(LivingEntity entity){
        return 1 / Math.max(1, 10 - (TriggerModule.getLuck(entity) / 3));
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, List<AttackContext> attCtxs) {
        LivingEntity entity = context.getEntity();
        for (var ctx : attCtxs) {
            if (entity.getRandom().nextDouble() < getTriggerChance(entity)){
                ctx.colorRl = ModBulletColor.CHARM.getId();
                ctx.getTrigger().addAll(bullet_triggers);
            }
        }
    }
}
