package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.minecraft.resources.ResourceLocation;

public class TriggerModuleInstance {
    public ResourceLocation id;
    public int stacks;
    public double priority;

    public TriggerModuleInstance(){}
    public TriggerModuleInstance(ResourceLocation id, int stacks, double priority){
        this.id = id;
        this.stacks = stacks;
        this.priority = priority;
    }
}
