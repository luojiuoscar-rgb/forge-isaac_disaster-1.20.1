package net.luojiuoscar.isaac_disaster.renderer.visual;

import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisualTarget;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/** Resolves synchronized visual candidates with the same priority rules used for bullet rendering. */
public final class BulletVisualResolver {
    private BulletVisualResolver() {
    }

    @Nullable
    public static BulletVisual resolve(TearBullet bullet, Predicate<BulletVisual> isRenderable) {
        Objects.requireNonNull(isRenderable, "isRenderable");
        IForgeRegistry<BulletVisual> registry = RegistryManager.ACTIVE.getRegistry(ModBulletVisuals.BULLET_VISUAL_KEY);
        if (registry == null) {
            return null;
        }

        BulletVisualTarget target = bullet instanceof FetusBullet
                ? BulletVisualTarget.FETUS
                : BulletVisualTarget.TEAR;
        ResourceLocation bestId = null;
        BulletVisual bestVisual = null;
        for (ResourceLocation id : bullet.getVisualIds()) {
            BulletVisual candidate = registry.getValue(id);
            if (candidate == null || candidate.getTarget() != target || !isRenderable.test(candidate)) {
                continue;
            }
            if (bestVisual == null
                    || candidate.getPriority() > bestVisual.getPriority()
                    || candidate.getPriority() == bestVisual.getPriority()
                    && id.toString().compareTo(bestId.toString()) < 0) {
                bestId = id;
                bestVisual = candidate;
            }
        }

        if (bestVisual != null) {
            return bestVisual;
        }
        ResourceLocation fallbackId = target == BulletVisualTarget.FETUS
                ? ModBulletVisuals.DEFAULT_FETUS.getId()
                : ModBulletVisuals.DEFAULT_TEAR.getId();
        BulletVisual fallback = registry.getValue(fallbackId);
        return fallback != null && isRenderable.test(fallback) ? fallback : null;
    }
}
