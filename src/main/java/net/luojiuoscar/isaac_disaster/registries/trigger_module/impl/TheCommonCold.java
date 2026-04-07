package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class TheCommonCold implements ITriggerModule {
    private static final CompositeTrigger triggers = new CompositeTrigger( List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY, ModAbilityEffects.THE_COMMON_COLD, context -> {
                LivingEntity entity = context.getEntity();
                return !(entity.getRandom().nextDouble() < TheCommonCold.getTriggerChance(entity));
            })
    ));

    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModAbilityEffects.THE_COMMON_COLD)
    );


    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }

    public static double getTriggerChance(LivingEntity entity){
        return 1 / Math.max(1, 4 - (ITriggerModule.getLuck(entity) / 4));
    }


    @Override
    public void attachToBullet(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();
        // 添加simpleTrigger到bullet中
        if (context.get(ContextKeys.EVENT) instanceof GetAttackContextEvent event) {
            List<AttackContext> attCtxs = event.getContexts();
            for (var ctx : attCtxs) {
                if (entity.getRandom().nextDouble() < getTriggerChance(entity)){
                    ctx.colorRl = ModBulletColor.POISON.getId();
                    ctx.getTriggers().addAll(bullet_triggers);
                }
            }
        }
    }
}
