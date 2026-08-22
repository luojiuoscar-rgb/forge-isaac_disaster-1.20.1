package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class IronBar extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.IRON_BAR, context ->
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
        return LuckTriggerChance.ironBar(TriggerModule.getLuck(entity));
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        LivingEntity entity = context.getEntity();
        if (entity.getRandom().nextDouble() < getTriggerChance(entity)){
            attackContext.getTrigger().addAll(BULLET_TRIGGER);
        }
    }
}
