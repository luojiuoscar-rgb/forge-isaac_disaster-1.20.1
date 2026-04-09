package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.minecraft.world.entity.LivingEntity;

public abstract class RecursiveModule {
    private final CompositeTrigger trigger;

    public RecursiveModule(CompositeTrigger trigger){
        this.trigger = trigger;
    }

    public int getInitialTick() {
        return 0;
    }

    public abstract int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue);

    /** 最终移除时触发 */
    public void handleRemove(LivingEntity entity){};

    /** 循环型在触发时，不进行类型判断 */
    public void fire(ExecutableEffectContext context){
        trigger.fire(context, null);
    }
}
