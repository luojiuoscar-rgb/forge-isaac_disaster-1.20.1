package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.resources.ResourceLocation;

/** Immutable ordered trajectory configuration captured when a bullet is created. */
public record TrajectorySpec(ResourceLocation id, int amplifier) {
    public TrajectorySpec {
        if (id == null) throw new IllegalArgumentException("trajectory id cannot be null");
    }

    /** Converts attached module stacks to the zero-based runtime amplifier. */
    public static TrajectorySpec fromStacks(ResourceLocation id, int stacks) {
        if (stacks <= 0) throw new IllegalArgumentException("stacks must be positive");
        return new TrajectorySpec(id, stacks - 1);
    }
}
