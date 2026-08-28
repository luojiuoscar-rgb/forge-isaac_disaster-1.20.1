package net.luojiuoscar.isaac_disaster.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletRenderContext;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletVisualClientRenderers;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisualTarget;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

public final class BulletRenderer extends EntityRenderer<TearBullet> {
    private final WeakHashMap<TearBullet, CachedVisual> cachedVisuals = new WeakHashMap<>();
    private final BulletVisualClientRenderers.Dispatcher visualRenderers;

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        visualRenderers = BulletVisualClientRenderers.createDispatcher(context);
    }

    @Override
    public void render(@NotNull TearBullet bullet, float entityYaw, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        ResolvedVisual visual = resolveCachedVisual(bullet);
        if (visual == null) {
            return;
        }
        visual.renderer().render(visual.visual(), new BulletRenderContext(bullet, partialTicks, packedLight), poseStack, buffer);
        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TearBullet entity) {
        ResolvedVisual visual = resolveCachedVisual(entity);
        if (visual == null) {
            return MissingTextureAtlasSprite.getLocation();
        }
        return visual.renderer().textureLocation(visual.visual(), entity);
    }

    @Nullable
    private ResolvedVisual resolveCachedVisual(TearBullet bullet) {
        String signature = bullet.getVisualIdsSignature();
        CachedVisual cached = cachedVisuals.get(bullet);
        if (cached != null && cached.signature().equals(signature)) {
            return cached.bestVisual();
        }

        ResolvedVisual bestVisual = resolveVisual(bullet);
        cachedVisuals.put(bullet, new CachedVisual(signature, bestVisual));
        return bestVisual;
    }

    private ResolvedVisual resolveVisual(TearBullet bullet) {
        IForgeRegistry<BulletVisual> registry = RegistryManager.ACTIVE.getRegistry(ModBulletVisuals.BULLET_VISUAL_KEY);
        if (registry == null) {
            IsaacDisaster.LOGGER.warn("Bullet visual registry is unavailable; skipping bullet render");
            return null;
        }

        BulletVisualTarget target = bullet instanceof FetusBullet
                ? BulletVisualTarget.FETUS
                : BulletVisualTarget.TEAR;
        ResourceLocation bestId = null;
        BulletVisual bestVisual = null;

        for (ResourceLocation id : bullet.getVisualIds()) {
            BulletVisual candidate = registry.getValue(id);
            if (candidate == null || candidate.getTarget() != target || !visualRenderers.hasRenderer(candidate)) {
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

        if (bestVisual == null) {
            ResourceLocation fallbackId = target == BulletVisualTarget.FETUS
                    ? ModBulletVisuals.DEFAULT_FETUS.getId()
                    : ModBulletVisuals.DEFAULT_TEAR.getId();
            bestVisual = registry.getValue(fallbackId);
            if (bestVisual == null || !visualRenderers.hasRenderer(bestVisual)) {
                IsaacDisaster.LOGGER.warn("Default bullet visual renderer is unavailable: {}; skipping bullet render", fallbackId);
                return null;
            }
        }

        BulletVisualClientRenderers.BoundRenderer renderer = visualRenderers.getRenderer(bestVisual);
        if (renderer == null) {
            IsaacDisaster.LOGGER.warn("Resolved bullet visual has no client renderer; skipping bullet render");
            return null;
        }
        return new ResolvedVisual(bestVisual, renderer);
    }

    private record CachedVisual(String signature, ResolvedVisual bestVisual) {
    }

    private record ResolvedVisual(BulletVisual visual, BulletVisualClientRenderers.BoundRenderer renderer) {
    }
}
