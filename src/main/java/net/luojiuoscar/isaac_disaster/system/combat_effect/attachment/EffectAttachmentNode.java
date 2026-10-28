package net.luojiuoscar.isaac_disaster.system.combat_effect.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryManager;

import java.util.function.Supplier;

/** Receives creation-time status rolls and tint without requiring a projectile. */
public interface EffectAttachmentNode {
    enum HitPhase { BEFORE_DAMAGE, AFTER_DAMAGE }

    void addStatusEffect(HitPhase phase, Supplier<? extends IExecutableEffect> effect);

    ResourceLocation getColorRl();

    void setColorRl(ResourceLocation color);

    /** Higher priority wins; equal priority preserves the existing tint. */
    default void offerColor(ResourceLocation color) {
        var registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (registry == null) return;
        var offered = registry.getValue(color);
        var current = getColorRl() == null ? null : registry.getValue(getColorRl());
        if (offered != null && (current == null || current.priority() < offered.priority())) {
            setColorRl(color);
        }
    }
}
