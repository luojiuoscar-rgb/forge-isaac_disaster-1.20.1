package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackPipelineMode;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackRequest;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Mutable per-bullet sequence of registered split modules. */
public final class SplitSequence {
    private final List<SplitModuleEntry> entries;

    public SplitSequence() {
        this.entries = new ArrayList<>();
    }

    private SplitSequence(List<SplitModuleEntry> entries) {
        this.entries = new ArrayList<>(entries);
        sort();
    }

    /** Adds stacks of a registered module, merging an existing entry by id. */
    public void add(@NotNull ResourceLocation moduleId, int stacks) {
        if (stacks <= 0) return;
        IForgeRegistry<SplitModule> registry = registry();
        SplitModule module = registry == null ? null : registry.getValue(moduleId);
        if (module == null) return;

        for (int i = 0; i < entries.size(); i++) {
            SplitModuleEntry entry = entries.get(i);
            if (entry.moduleId.equals(moduleId)) {
                entries.set(i, new SplitModuleEntry(moduleId, entry.stacks + stacks,
                        module.getPriority(), entry.triggerCount));
                sort();
                return;
            }
        }
        entries.add(new SplitModuleEntry(moduleId, stacks, module.getPriority(), 0));
        sort();
    }

    /** Removes all entries for a registered module. */
    public void remove(@NotNull ResourceLocation moduleId) {
        entries.removeIf(entry -> entry.moduleId.equals(moduleId));
    }

    public boolean contains(@NotNull ResourceLocation moduleId) {
        return entries.stream().anyMatch(entry -> entry.moduleId.equals(moduleId));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public SplitSequence copy() {
        return new SplitSequence(entries);
    }

    /** Returns a copy with the current module's runtime trigger count advanced. */
    public SplitSequence copyWithIncrementedTriggerCount(@NotNull ResourceLocation moduleId) {
        SplitSequence copy = copy();
        for (int i = 0; i < copy.entries.size(); i++) {
            SplitModuleEntry entry = copy.entries.get(i);
            if (entry.moduleId.equals(moduleId)) {
                copy.entries.set(i, new SplitModuleEntry(entry.moduleId, entry.stacks,
                        entry.priority, entry.triggerCount + 1));
                break;
            }
        }
        return copy;
    }

    /**
     * Creates a child sequence by asking every registered module whether it inherits this child.
     * Each retained entry preserves its independent runtime data.
     */
    public SplitSequence copyForChild(@NotNull SplitContext context, @NotNull AttackContext childContext,
                                      boolean inheritModules) {
        if (!inheritModules) return new SplitSequence();
        IForgeRegistry<SplitModule> registry = registry();
        if (registry == null) return new SplitSequence();

        List<SplitModuleEntry> childEntries = new ArrayList<>();
        for (SplitModuleEntry entry : entries) {
            SplitModule module = registry.getValue(entry.moduleId);
            if (module == null) continue;

            SplitContext entryContext = new SplitContext(
                    context.getEvent(), entry.moduleId, module, entry.stacks, entry.triggerCount);
            if (module.shouldInherit(entryContext, childContext)) {
                childEntries.add(entry);
            }
        }
        return new SplitSequence(childEntries);
    }

    /** Resolves one highest applicable priority layer and builds child requests. */
    public List<AttackRequest> createChildRequests(@NotNull BulletSplitEvent event) {
        IForgeRegistry<SplitModule> registry = registry();
        if (registry == null || entries.isEmpty()) return List.of();

        for (double priority : priorities()) {
            List<SplitContext> applicable = new ArrayList<>();
            for (SplitModuleEntry entry : entries) {
                if (Double.compare(entry.priority, priority) != 0) continue;
                SplitModule module = registry.getValue(entry.moduleId);
                if (module == null) continue;

                SplitContext context = new SplitContext(
                        event, entry.moduleId, module, entry.stacks, entry.triggerCount);
                if (module.canTrigger(context)) {
                    applicable.add(context);
                }
            }
            if (applicable.isEmpty()) continue;

            Map<AttackType, List<AttackContext>> grouped = new LinkedHashMap<>();
            for (SplitContext context : applicable) {
                SplitModule module = context.getModule();
                List<AttackContext> children = new ArrayList<>(module.generate(context));
                module.applyInheritance(context, children);
                if (children.isEmpty()) continue;

                AttackType childType = module.resolveChildAttackType(context);
                children.replaceAll(child -> child.bindAttackTypeOrCopy(childType));
                grouped.computeIfAbsent(childType, ignored -> new ArrayList<>()).addAll(children);
            }

            List<AttackRequest> requests = new ArrayList<>();
            for (Map.Entry<AttackType, List<AttackContext>> group : grouped.entrySet()) {
                if (group.getValue().isEmpty()) continue;
                requests.add(AttackRequest.withContexts(
                        event.getParent().getOwner(), group.getKey(), AttackOrigin.SPLIT_CHILD,
                        AttackPipelineMode.EXECUTE_ONLY, group.getValue(), false));
            }
            return List.copyOf(requests);
        }
        return List.of();
    }

    private List<Double> priorities() {
        return entries.stream().map(SplitModuleEntry::priority).distinct().toList();
    }

    private void sort() {
        entries.sort(Comparator.comparingDouble(SplitModuleEntry::priority).reversed());
    }

    private static IForgeRegistry<SplitModule> registry() {
        return RegistryManager.ACTIVE.getRegistry(ModSplitModules.SPLIT_MODULE_KEY);
    }

    private record SplitModuleEntry(ResourceLocation moduleId, int stacks,
                                    double priority, int triggerCount) {
    }
}
