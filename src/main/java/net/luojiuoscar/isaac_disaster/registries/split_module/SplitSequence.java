package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
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

/** Runtime sequence of registered split modules ordered from highest to lowest priority. */
public final class SplitSequence {
    // TODO: Define how module stacks affect applicability, generation, and inheritance.
    private final List<Entry> entries;

    public SplitSequence() {
        this.entries = new ArrayList<>();
    }

    private SplitSequence(List<Entry> entries) {
        this.entries = new ArrayList<>(entries);
        sort();
    }

    /** Adds stacks of a registered module to this sequence. */
    public void add(@NotNull ResourceLocation moduleId, int stacks) {
        if (stacks <= 0) return;
        IForgeRegistry<SplitModule> registry = registry();
        SplitModule module = registry == null ? null : registry.getValue(moduleId);
        if (module == null) return;

        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.moduleId.equals(moduleId)) {
                entries.set(i, new Entry(moduleId, entry.stacks + stacks, module.getPriority()));
                sort();
                return;
            }
        }
        entries.add(new Entry(moduleId, stacks, module.getPriority()));
        sort();
    }

    /** Removes all entries for a registered module. */
    public void remove(@NotNull ResourceLocation moduleId) {
        entries.removeIf(entry -> entry.moduleId.equals(moduleId));
    }

    /** Returns whether the sequence has no module entries. */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /** Returns an independent copy of this runtime sequence. */
    public SplitSequence copy() {
        return new SplitSequence(entries);
    }

    /** Resolves the first priority layer with at least one applicable module. */
    public List<AttackRequest> createChildRequests(@NotNull BulletSplitEvent event) {
        IForgeRegistry<SplitModule> registry = registry();
        if (registry == null || entries.isEmpty()) return List.of();

        for (double priority : priorities()) {
            List<ResolvedModule> applicable = new ArrayList<>();
            for (Entry entry : entries) {
                if (Double.compare(entry.priority, priority) != 0) continue;
                SplitModule module = registry.getValue(entry.moduleId);
                if (module == null || !module.isApplicable(event)) continue;
                if (!SplitRuleCache.allows(event, entry.moduleId, module)) continue;
                applicable.add(new ResolvedModule(module));
            }
            if (applicable.isEmpty()) continue;

            SplitSequence childSequence = lowerPrioritySequence(priority);
            Map<AttackType, List<AttackContext>> grouped = new LinkedHashMap<>();
            for (ResolvedModule resolved : applicable) {
                List<AttackContext> contexts = resolved.module.generate(event, childSequence);
                grouped.computeIfAbsent(resolved.module.getChildAttackType(), ignored -> new ArrayList<>())
                        .addAll(contexts);
            }

            List<AttackRequest> requests = new ArrayList<>();
            for (Map.Entry<AttackType, List<AttackContext>> group : grouped.entrySet()) {
                requests.add(AttackRequest.withContexts(
                        event.getParent().getOwner(), group.getKey(), AttackOrigin.SPLIT_CHILD,
                        AttackPipelineMode.BULLET_ONLY, group.getValue(), false));
            }
            return List.copyOf(requests);
        }
        return List.of();
    }

    private SplitSequence lowerPrioritySequence(double priority) {
        return new SplitSequence(entries.stream()
                .filter(entry -> Double.compare(entry.priority, priority) < 0)
                .toList());
    }

    private List<Double> priorities() {
        return entries.stream().map(entry -> entry.priority).distinct().toList();
    }

    private void sort() {
        entries.sort(Comparator.comparingDouble((Entry entry) -> entry.priority).reversed());
    }

    private static IForgeRegistry<SplitModule> registry() {
        return RegistryManager.ACTIVE.getRegistry(ModSplitModules.SPLIT_MODULE_KEY);
    }

    private record Entry(ResourceLocation moduleId, int stacks, double priority) {
    }

    private record ResolvedModule(SplitModule module) {
    }
}
