package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.minecraft.world.entity.LivingEntity;

public interface IRecursiveModule {

    CompositeTrigger getTriggers();

    default int getInitialTick() {
        return 0;
    }

    int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue);

    /** 最终移除时触发 */
    default void handleRemove(LivingEntity entity){};

    /** 循环型在触发时，不进行类型判断 */
    default void fire(AbilityEffectContext context){
        getTriggers().fire(context, null);
    }
}
