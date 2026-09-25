package net.luojiuoscar.isaac_disaster.registries.trajectory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/** Creation-time module stacks in first-attachment order. */
public final class TrajectorySequence {
    private final LinkedHashMap<ResourceLocation, Integer> stacks = new LinkedHashMap<>();
    private boolean resolved;
    private ResourceLocation resolvedType;

    public void add(ResourceLocation moduleId, int count) {
        Objects.requireNonNull(moduleId, "moduleId");
        if (count > 0) {
            stacks.merge(moduleId, count, Math::addExact);
            resolved = false;
        }
    }

    public TrajectorySequence copy() {
        TrajectorySequence copy = new TrajectorySequence();
        copy.stacks.putAll(stacks);
        copy.resolved = resolved;
        copy.resolvedType = resolvedType;
        return copy;
    }

    public List<TrajectorySpec> snapshot() {
        return stacks.entrySet().stream()
            .map(entry -> TrajectorySpec.fromStacks(entry.getKey(), entry.getValue()))
            .toList();
    }

    public void resolve(ResourceLocation type) {
        if (resolved && Objects.equals(resolvedType, type)) return;
        List<TrajectorySpec> result =
            net.luojiuoscar.isaac_disaster.registries.trajectory.rule.TrajectoryRules.resolve(
                type, snapshot());
        stacks.clear();
        result.forEach(spec -> stacks.put(spec.id(), spec.amplifier() + 1));
        resolved = true;
        resolvedType = type;
    }
}
