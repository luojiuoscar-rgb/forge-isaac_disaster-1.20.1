package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.minecraft.resources.ResourceLocation;

/**
 * Immutable result of choosing the player's current attack type.
 *
 * <p>The stored priority is the selected candidate priority, not necessarily the base priority of
 * the resulting attack type. Combination rules can therefore make a delegating attack search for a
 * lower attack from the combination's position in the priority order.</p>
 */
public record AttackSelection(ResourceLocation attackTypeId, AttackType attackType,
                              int priorityTier, double priority) {
}
