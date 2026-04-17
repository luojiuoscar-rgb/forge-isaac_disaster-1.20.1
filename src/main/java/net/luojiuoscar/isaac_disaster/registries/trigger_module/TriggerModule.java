package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public abstract class TriggerModule {
    private final CompositeTrigger trigger;

    public TriggerModule(CompositeTrigger trigger){
        this.trigger = trigger;
    }

    public void attachToBullet(ExecutableEffectContext context, List<AttackContext> attCtxs){}

    // 执行所有fire
    public void fire(ExecutableEffectContext context, TriggerType type){
        // 如果需要附加给子弹
        if (type.is(ModTriggerTypes.GET_ATTACK_CONTEXT)
                && context.get(ContextKeys.EVENT) instanceof GetAttackContextEvent event){
            attachToBullet(context, event.getContexts());
        }

        trigger.fire(context, type);
    }

    public double getPriority() {return 0; }

    /** 每一次获取模块的时候都会触发 */
    public void onAdded(LivingEntity entity, TriggerModuleQueue queue){}

    /** 每一次失去该模块的时候都会触发 */
    public void onRemove(LivingEntity entity, TriggerModuleQueue queue){}

    protected static double getLuck(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        return instance == null ? 0.0 : instance.getValue();
    }
}
