package net.luojiuoscar.isaac_disaster.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletRenderContext;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletVisualClientRenderers;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletVisualResolver;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
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
        BulletVisual bestVisual = BulletVisualResolver.resolve(bullet, visualRenderers::hasRenderer);
        if (bestVisual == null || !visualRenderers.hasRenderer(bestVisual)) {
            return null;
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
