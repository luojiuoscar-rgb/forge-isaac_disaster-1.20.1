package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class IronBar extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY, ModExecutableEffects.IRON_BAR, context ->
                            context.getEntity().getRandom().nextDouble()
                                    < getTriggerChance(context.getEntity()))
    ));

    private static final List<SimpleTrigger> BULLET_TRIGGER = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.IRON_BAR)
    );

    public IronBar() {
        super(TRIGGER);
    }

    public static double getTriggerChance(LivingEntity entity){
        return 1 / Math.max(1, 10 - (TriggerModule.getLuck(entity) / 3));
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        // 添加simpleTrigger到bullet中
        if (context.get(ContextKeys.EVENT) instanceof GetAttackContextEvent event) {
            List<AttackContext> attCtxs = event.getContexts();
            for (var ctx : attCtxs) {
                if (entity.getRandom().nextDouble() < getTriggerChance(entity)){
                    ctx.getTrigger().addAll(BULLET_TRIGGER);
                }
            }
        }
    }
}
