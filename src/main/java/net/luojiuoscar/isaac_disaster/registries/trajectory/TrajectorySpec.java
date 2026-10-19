package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.resources.ResourceLocation;

/** Immutable ordered trajectory configuration captured when a bullet is created. */
public record TrajectorySpec(ResourceLocation id, int amplifier) {
    public TrajectorySpec {
        if (id == null) throw new IllegalArgumentException("trajectory id cannot be null");
        amplifier = Math.max(0, Math.min(Integer.MAX_VALUE - 1, amplifier));
    }

    /** Converts attached module stacks to the zero-based runtime amplifier. */
    public static TrajectorySpec fromStacks(ResourceLocation id, int stacks) {
        return new TrajectorySpec(id, Math.max(1, stacks) - 1);
    }
}
