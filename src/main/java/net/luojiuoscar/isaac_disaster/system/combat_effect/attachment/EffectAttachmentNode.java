package net.luojiuoscar.isaac_disaster.system.combat_effect.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.function.Supplier;

/** Receives creation-time status rolls and tint without requiring a projectile. */
public interface EffectAttachmentNode {
    enum HitPhase { BEFORE_DAMAGE, AFTER_DAMAGE }

    void addStatusEffect(HitPhase phase, Supplier<? extends IExecutableEffect> effect);

    void offerColor(ResourceLocation color);

    default void offerColors(Collection<ResourceLocation> colors) {
        colors.forEach(this::offerColor);
    }
}
