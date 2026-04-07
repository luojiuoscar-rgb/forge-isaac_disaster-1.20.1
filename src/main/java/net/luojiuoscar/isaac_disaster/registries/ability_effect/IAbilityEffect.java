package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public interface IAbilityEffect extends IExecutableEffect {

    /** 执行效果，如果失败且错误效果开启，可以执行一个随机的弱化后的效果（或固定弱化效果） */
    default void apply(AbilityEffectContext context){
        if (context.getEntity().level().isClientSide) return;

        // 使用try确保不会崩溃
        boolean success = true;
        try {
            success = applyEffect(context);
        }catch (SilentException ignored){
            success = false;
        }

        // default behaviour
        if (!success){
            //TODO
        }

    }
    

    boolean applyEffect(AbilityEffectContext context);


    default double getLuck(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        return instance == null ? 0.0 : instance.getValue();
    }
}
