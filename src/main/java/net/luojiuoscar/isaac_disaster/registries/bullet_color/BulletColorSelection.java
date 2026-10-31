package net.luojiuoscar.isaac_disaster.registries.bullet_color;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/** Creation-time candidates, resolved only after the actual attack type is known. */
public final class BulletColorSelection {
    private final Set<ResourceLocation> candidates = new HashSet<>();

    public BulletColorSelection() {}
    public BulletColorSelection(Collection<ResourceLocation> ids) { candidates.addAll(ids); }
    public boolean offer(ResourceLocation id) { return candidates.add(Objects.requireNonNull(id, "color ID")); }
    public Set<ResourceLocation> ids() { return Set.copyOf(candidates); }
    public BulletColorSelection copy() { return new BulletColorSelection(candidates); }

    public Result resolveForAttack(@Nullable AttackType type, int defaultRgb, boolean applyAlpha) {
        var registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        return resolve(type == null ? null : type.getId(), type == null ? null : type.getRootId(),
                defaultRgb, applyAlpha, registry == null ? id -> null : registry::getValue);
    }

    public Result resolveForGroundEffect(int defaultRgb) {
        return resolveForAttack(null, defaultRgb, false);
    }

    Result resolve(@Nullable ResourceLocation type, @Nullable ResourceLocation root, int defaultRgb,
                   boolean applyAlpha, Function<ResourceLocation, BulletColor> lookup) {
        ResourceLocation bestId = ModBulletColors.BASE.getId();
        BulletColor best = null;
        float alpha = 1;
        for (ResourceLocation id : candidates) {
            BulletColor color = lookup.apply(id);
            if (color == null || !color.appliesTo(type, root)) continue;
            if (applyAlpha) alpha = Math.min(alpha, color.alpha());
            // BASE is a neutral request for the consumer's own default color.
            if (color.color() == null || id.equals(ModBulletColors.BASE.getId())) continue;
            if (best == null || preferred(color, id, best, bestId)) {
                best = color;
                bestId = id;
            }
        }
        return new Result(bestId, best == null ? defaultRgb : best.color(), alpha);
    }

    private static boolean preferred(BulletColor a, ResourceLocation aId, BulletColor b, ResourceLocation bId) {
        int tier = Integer.compare(a.priorityTier(), b.priorityTier());
        if (tier != 0) return tier > 0;
        int priority = Double.compare(a.priority(), b.priority());
        return priority != 0 ? priority > 0 : aId.toString().compareTo(bId.toString()) < 0;
    }

    public record Result(ResourceLocation colorId, int color, float alpha) {}
}
