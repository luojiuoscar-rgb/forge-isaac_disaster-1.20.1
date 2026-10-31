package net.luojiuoscar.isaac_disaster.registries.bullet_color;

import org.joml.Vector3f;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public record BulletColor(@Nullable Integer color, float alpha, int priorityTier, double priority,
                          Set<ResourceLocation> attackTypes, Set<ResourceLocation> rootTypes) {
    public BulletColor {
        color = color == null ? null : color & 0xFFFFFF;
        alpha = Float.isFinite(alpha) ? Math.max(0.5F, Math.min(1, alpha)) : 1;
        priority = Double.isFinite(priority) ? priority : 0;
        attackTypes = Set.copyOf(attackTypes);
        rootTypes = Set.copyOf(rootTypes);
    }

    public static Builder builder() { return new Builder(); }

    public boolean appliesTo(@Nullable ResourceLocation type, @Nullable ResourceLocation root) {
        return attackTypes.isEmpty() && rootTypes.isEmpty()
                || type != null && attackTypes.contains(type) || root != null && rootTypes.contains(root);
    }

    public static final class Builder {
        private Integer color;
        private float alpha = 1;
        private int tier;
        private double priority;
        private final Set<ResourceLocation> types = new HashSet<>();
        private final Set<ResourceLocation> roots = new HashSet<>();

        public Builder color(int rgb) { color = rgb; return this; }
        public Builder alpha(float value) { alpha = value; return this; }
        public Builder priority(int valueTier, double valuePriority) {
            tier = valueTier; priority = valuePriority; return this;
        }
        public Builder priority(BulletColorPrio preset) {
            return priority(preset.getTier(), preset.getPriority());
        }
        public Builder onlyTypes(ResourceLocation... ids) { types.addAll(Arrays.asList(ids)); return this; }
        public Builder onlyRoots(ResourceLocation... ids) { roots.addAll(Arrays.asList(ids)); return this; }
        @SafeVarargs
        public final Builder onlyTypes(RegistryObject<? extends AttackType>... entries) {
            for (var entry : entries) types.add(entry.getId());
            return this;
        }
        @SafeVarargs
        public final Builder onlyRoots(RegistryObject<? extends AttackType>... entries) {
            for (var entry : entries) roots.add(entry.getId());
            return this;
        }
        public BulletColor build() { return new BulletColor(color, alpha, tier, priority, types, roots); }
    }

    public static Vector3f getVec3fColorById(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new Vector3f(r, g, b);
    }
}
