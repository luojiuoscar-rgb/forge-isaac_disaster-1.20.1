package net.luojiuoscar.isaac_disaster.renderer.visual;

import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BulletVisualRegistry {
    private static final Map<ResourceLocation, BulletVisual> VISUALS = new HashMap<>();

    private BulletVisualRegistry() {
    }

    public static void registerDefaults(EntityRendererProvider.Context context) {
        if (!VISUALS.containsKey(TearBullet.DEFAULT_VISUAL_ID)) {
            register(TearBullet.DEFAULT_VISUAL_ID, new DefaultTearVisual());
        }
        if (!VISUALS.containsKey(FetusBullet.DEFAULT_VISUAL_ID)) {
            register(FetusBullet.DEFAULT_VISUAL_ID, new DefaultFetusVisual(context));
        }
    }

    public static void register(ResourceLocation id, BulletVisual visual) {
        ResourceLocation requiredId = Objects.requireNonNull(id, "id");
        BulletVisual requiredVisual = Objects.requireNonNull(visual, "visual");
        if (VISUALS.putIfAbsent(requiredId, requiredVisual) != null) {
            throw new IllegalStateException("Duplicate bullet visual: " + requiredId);
        }
    }

    public static BulletVisual resolve(TearBullet bullet) {
        BulletVisualTarget target = BulletVisualTarget.of(bullet);
        ResourceLocation bestId = null;
        BulletVisual bestVisual = null;

        for (ResourceLocation id : bullet.getVisualIds()) {
            BulletVisual candidate = VISUALS.get(id);
            if (candidate == null || candidate.target() != target) {
                continue;
            }
            if (bestVisual == null
                    || candidate.priority() > bestVisual.priority()
                    || candidate.priority() == bestVisual.priority()
                    && id.toString().compareTo(bestId.toString()) < 0) {
                bestId = id;
                bestVisual = candidate;
            }
        }
        if (bestVisual != null) {
            return bestVisual;
        }

        ResourceLocation fallbackId = target == BulletVisualTarget.FETUS
                ? FetusBullet.DEFAULT_VISUAL_ID
                : TearBullet.DEFAULT_VISUAL_ID;
        BulletVisual fallback = VISUALS.get(fallbackId);
        if (fallback == null) {
            throw new IllegalStateException("Missing default bullet visual: " + fallbackId);
        }
        return fallback;
    }
}
