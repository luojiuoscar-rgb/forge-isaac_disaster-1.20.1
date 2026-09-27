package net.luojiuoscar.isaac_disaster.registries.trajectory.rule;

import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Pure creation-time rewrite. Inputs are consumed; empty output deletes them. Output must depend
 * only on the immutable matched stacks, never world state.
 */
public abstract class TrajectoryRule {
    private final Set<ResourceLocation> inputs;
    private final ResourceLocation sourceType;
    private final int priority;

    protected TrajectoryRule(
        Set<ResourceLocation> inputs, ResourceLocation sourceType, int priority) {
        this.inputs = Set.copyOf(inputs);
        if (this.inputs.isEmpty()) throw new IllegalArgumentException("rule must require a trajectory");
        this.sourceType = sourceType;
        this.priority = priority;
    }

    public final Set<ResourceLocation> inputs() {
        return inputs;
    }

    public final boolean accepts(ResourceLocation type) {
        return sourceType == null || sourceType.equals(type);
    }

    public final int priority() {
        return priority;
    }

    public abstract Optional<TrajectorySpec> output(Map<ResourceLocation, Integer> matchedStacks);

    /** Existing output keeps its stacks; a new output receives the minimum input stacks. */
    public static TrajectoryRule replace(Set<ResourceLocation> inputs,
                                            ResourceLocation output,
                                            ResourceLocation sourceType,
                                            int priority) {
        return new TrajectoryRule(inputs, sourceType, priority) {
            @Override
            public Optional<TrajectorySpec> output(Map<ResourceLocation, Integer> stacks) {
                if (output == null) return Optional.empty();
                int count =
                    stacks.getOrDefault(
                        output, stacks.values().stream().mapToInt(Integer::intValue).min().orElseThrow());
                return Optional.of(TrajectorySpec.fromStacks(Objects.requireNonNull(output), count));
            }
        };
    }
}
