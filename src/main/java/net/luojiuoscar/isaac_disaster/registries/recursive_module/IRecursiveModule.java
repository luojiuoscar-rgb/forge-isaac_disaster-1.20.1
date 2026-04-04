package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface IRecursiveModule {

    List<SimpleTrigger> getTriggers();

    default void modifyContext(AbilityEffectContext context){
    }

    default int getInitialTick() {
        return 0;
    }

    int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue);

    /** 最终移除时触发 */
    default void handleRemove(LivingEntity entity){};

    default void fire(AbilityEffectContext context){
        modifyContext(context); // 优先额外修改内容

        for (SimpleTrigger effect : getTriggers()){
            effect.fire(context);
        }
    }
}
