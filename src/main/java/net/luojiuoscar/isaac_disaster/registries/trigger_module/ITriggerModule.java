package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public interface ITriggerModule {

    /** 不要在这里使用new */
    List<SimpleTrigger> getTriggers();

    default void modifyContext(AbilityEffectContext context){}

    /** 也不需要频繁new */
    default void attachToBullet(AbilityEffectContext context){}

    // 执行所有fire
    default void fire(AbilityEffectContext context, TriggerType type){
        modifyContext(context); // 优先额外修改内容

        // 如果需要附加给子弹
        if (type.is(ModTriggerTypes.GET_ATTACK_CONTEXT)){
            attachToBullet(context);
        }

        for (SimpleTrigger trigger : getTriggers()){
            if (trigger.getType().is(type)){
                trigger.fire(context);
            }
        }
    }

    default double getLuck(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        return instance == null ? 0.0 : instance.getValue();
    }

    default double getPriority() {return 0; }

    /** 每一次获取模块的时候都会触发 */
    default void onAdded(LivingEntity entity, TriggerModuleQueue queue){}

    /** 每一次失去该模块的时候都会触发 */
    default void onRemove(LivingEntity entity, TriggerModuleQueue queue){}
}
