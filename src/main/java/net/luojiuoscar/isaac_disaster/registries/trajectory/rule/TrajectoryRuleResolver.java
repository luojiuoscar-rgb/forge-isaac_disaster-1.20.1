package net.luojiuoscar.isaac_disaster.registries.trajectory.rule;

import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/** Compiled inverse index. Each rule fires at most once, including cyclic rewrites. */
public final class TrajectoryRuleResolver {
    private record Entry(ResourceLocation id, TrajectoryRule rule) {
    }

    private record Key(ResourceLocation type, List<TrajectorySpec> specs) {
    }

    private final List<Entry> rules;
    private final Map<ResourceLocation, List<Integer>> index = new HashMap<>();
    private final Map<Key, List<TrajectorySpec>> cache =
        new LinkedHashMap<>(64, .75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Key, List<TrajectorySpec>> entry) {
                return size() > 1024;
            }
        };

    public TrajectoryRuleResolver(Map<ResourceLocation, TrajectoryRule> registered) {
        rules = registered.entrySet().stream()
                .map(e -> new Entry(e.getKey(), e.getValue()))
                .sorted(
                    Comparator.comparingInt((Entry e) -> e.rule.priority())
                        .thenComparing(e -> e.id.toString()))
                .toList();
        for (int i = 0; i < rules.size(); i++) {
            for (ResourceLocation input : rules.get(i).rule.inputs())
                index.computeIfAbsent(input, k -> new ArrayList<>()).add(i);
        }
    }

    public synchronized List<TrajectorySpec> resolve(
        ResourceLocation type, List<TrajectorySpec> input) {
        Key key = new Key(type, List.copyOf(input));
        List<TrajectorySpec> cached = cache.get(key);
        if (cached != null) return cached;
        LinkedHashMap<ResourceLocation, Integer> stacks = new LinkedHashMap<>();
        for (TrajectorySpec spec : input)
            stacks.merge(spec.id(), Math.addExact(spec.amplifier(), 1), Math::addExact);
        TreeSet<Integer> candidates = new TreeSet<>();
        stacks.keySet().forEach(id -> candidates.addAll(index.getOrDefault(id, List.of())));
        BitSet applied = new BitSet(rules.size());
        while (!candidates.isEmpty()) {
            int i = candidates.pollFirst();
            TrajectoryRule rule = rules.get(i).rule;
            if (applied.get(i) || !rule.accepts(type) || !stacks.keySet().containsAll(rule.inputs()))
                continue;
            LinkedHashMap<ResourceLocation, Integer> matched = new LinkedHashMap<>();
            for (ResourceLocation id : rule.inputs()) matched.put(id, stacks.get(id));
            Optional<TrajectorySpec> output =
                Objects.requireNonNull(rule.output(Collections.unmodifiableMap(matched)));
            applied.set(i);
            LinkedHashMap<ResourceLocation, Integer> next = new LinkedHashMap<>();
            boolean inserted = false;
            for (var entry : stacks.entrySet()) {
                if (rule.inputs().contains(entry.getKey())) {
                    if (!inserted && output.isPresent()) {
                        TrajectorySpec spec = output.get();
                        int count = Math.addExact(spec.amplifier(), 1);
                        if (count <= 0)
                            throw new IllegalArgumentException("invalid rule output stacks: " + rules.get(i).id);
                        next.merge(spec.id(), count, Math::addExact);
                        inserted = true;
                    }
                } else next.merge(entry.getKey(), entry.getValue(), Math::addExact);
            }
            stacks = next;
            // Only newly enabled rules need revisiting. Removed IDs cannot enable a match.
            output.ifPresent(spec -> candidates.addAll(index.getOrDefault(spec.id(), List.of())));
        }
        List<TrajectorySpec> result =
            stacks.entrySet().stream()
                .map(e -> TrajectorySpec.fromStacks(e.getKey(), e.getValue()))
                .toList();
        cache.put(key, result);
        return result;
    }
}
