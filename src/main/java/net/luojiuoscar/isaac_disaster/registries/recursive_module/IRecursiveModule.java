package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.minecraft.world.entity.LivingEntity;

public interface IRecursiveModule {

    default int getInitialTick() {
        return 0;
    }

    int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue);

    /** 最终移除时触发 */
    default void handleRemove(LivingEntity entity){};

    void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue);
}
