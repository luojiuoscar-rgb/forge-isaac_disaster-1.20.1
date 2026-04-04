package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class TheCommonCold implements ITriggerModule {
    private static final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY, ModAbilityEffects.POTION)
    );
    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModAbilityEffects.POTION)
    );


    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }

    private double getTriggerChance(LivingEntity entity){
        return 1 / Math.max(1, 4 - (getLuck(entity) / 4));
    }

    @Override
    public void modifyContext(AbilityEffectContext context) {
        LivingEntity entity = context.getEntity();
        if (entity.getRandom().nextDouble() < getTriggerChance(entity)){
            context.set(ContextKeys.POTIONS, List.of(
                    new PotionProfile(ModEffects.POISON.get(), 70, 0)
            ));
        }
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
