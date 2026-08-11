package net.luojiuoscar.isaac_disaster.registries.visual;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Common definition for an entity visual state. */
public record VisualLayer(@Nullable ResourceLocation group, int conflictPriority) {
}
