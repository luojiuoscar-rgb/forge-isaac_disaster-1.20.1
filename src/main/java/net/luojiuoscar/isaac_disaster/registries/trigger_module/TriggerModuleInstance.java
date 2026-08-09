package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.minecraft.resources.ResourceLocation;

public record TriggerModuleInstance(ResourceLocation id, int stacks, double priority) {
}
