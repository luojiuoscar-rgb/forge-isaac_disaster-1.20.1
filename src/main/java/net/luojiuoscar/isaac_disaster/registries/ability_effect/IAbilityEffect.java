package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface IAbilityEffect {

    /** 执行效果，如果失败且错误效果开启，可以执行一个随机的弱化后的效果（或固定弱化效果） */
    default void apply(AbilityEffectContext context){
        boolean success = applyEffect(context);

        // default behaviour
    }
    

    boolean applyEffect(AbilityEffectContext context);


    default double getLuck(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        return instance == null ? 0.0 : instance.getValue();
    }
}
