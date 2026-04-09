package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface ITriggerModule {

    /** 不要在这里使用new */
    CompositeTrigger getTriggers();

    /** 也不需要频繁new */
    default void attachToBullet(ExecutableEffectContext context){}

    // 执行所有fire
    default void fire(ExecutableEffectContext context, TriggerType type){
        // 如果需要附加给子弹
        if (type.is(ModTriggerTypes.GET_ATTACK_CONTEXT)){
            attachToBullet(context);
        }

        getTriggers().fire(context, type);
    }

    default double getPriority() {return 0; }

    /** 每一次获取模块的时候都会触发 */
    default void onAdded(LivingEntity entity, TriggerModuleQueue queue){}

    /** 每一次失去该模块的时候都会触发 */
    default void onRemove(LivingEntity entity, TriggerModuleQueue queue){}

    static double getLuck(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        return instance == null ? 0.0 : instance.getValue();
    }
}
