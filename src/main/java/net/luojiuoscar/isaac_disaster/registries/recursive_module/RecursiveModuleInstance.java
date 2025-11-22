package net.luojiuoscar.isaac_disaster.registries.recursive_module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class RecursiveModuleInstance {
    public ResourceLocation id;
    public int stacks;
    public long nextPos;

    public RecursiveModuleInstance(ResourceLocation id, int stacks, long nextPos){
        this.id = id;
        this.stacks = stacks;
        this.nextPos = nextPos;
    }

    public void trigger(LivingEntity entity, RecursiveModuleQueue queue){
        IForgeRegistry<IRecursiveModule> registry =
                RegistryManager.ACTIVE.getRegistry(ModRecursiveModule.RECURSIVE_MODULE_KEY);
        if (registry == null) return;

        IRecursiveModule inst = registry.getValue(id);
        if (inst == null) return;

        inst.recursiveEffect(entity, stacks, queue);

        nextPos += inst.getTickInterval(entity, stacks, queue);
    }
}
