package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable, priority-ordered view of the active trigger modules for one dispatch.
 */
public final class TriggerModuleSnapshot {
    private static final TriggerModuleSnapshot EMPTY = new TriggerModuleSnapshot(List.of());

    private final List<TriggerModuleInstance> modules;
    private final Map<ResourceLocation, Integer> stacksById;

    public TriggerModuleSnapshot(List<TriggerModuleInstance> modules) {
        this.modules = List.copyOf(modules);

        Map<ResourceLocation, Integer> stacks = new LinkedHashMap<>();
        for (TriggerModuleInstance module : modules) {
            stacks.merge(module.id(), module.stacks(), Integer::sum);
        }
        this.stacksById = Map.copyOf(stacks);
    }

    public static TriggerModuleSnapshot empty() {
        return EMPTY;
    }

    public List<TriggerModuleInstance> modules() {
        return modules;
    }

    public boolean contains(ResourceLocation id) {
        return getStacks(id) > 0;
    }

    public int getStacks(ResourceLocation id) {
        return stacksById.getOrDefault(id, 0);
    }

    public boolean isEmpty() {
        return modules.isEmpty();
    }
}
