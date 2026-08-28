package net.luojiuoscar.isaac_disaster.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletRenderContext;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletVisual;
import net.luojiuoscar.isaac_disaster.renderer.visual.BulletVisualRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.WeakHashMap;

public final class BulletRenderer extends EntityRenderer<TearBullet> {
    private final WeakHashMap<TearBullet, CachedVisual> cachedVisuals = new WeakHashMap<>();

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        BulletVisualRegistry.registerDefaults(context);
    }

    @Override
    public void render(@NotNull TearBullet bullet, float entityYaw, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        resolveCachedVisual(bullet).render(new BulletRenderContext(bullet, partialTicks, packedLight), poseStack, buffer);
        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull TearBullet entity) {
        return resolveCachedVisual(entity).textureLocation(entity);
    }

    private BulletVisual resolveCachedVisual(TearBullet bullet) {
        String signature = bullet.getVisualIdsSignature();
        CachedVisual cached = cachedVisuals.get(bullet);
        if (cached != null && cached.signature().equals(signature)) {
            return cached.bestVisual();
        }

        BulletVisual bestVisual = BulletVisualRegistry.resolve(bullet);
        cachedVisuals.put(bullet, new CachedVisual(signature, bestVisual));
        return bestVisual;
    }

    private record CachedVisual(String signature, BulletVisual bestVisual) {
    }
}
