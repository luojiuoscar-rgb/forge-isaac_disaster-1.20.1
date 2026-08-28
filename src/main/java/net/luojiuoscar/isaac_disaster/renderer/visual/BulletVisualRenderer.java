package net.luojiuoscar.isaac_disaster.renderer.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/** Client-only renderer adapter for one common {@link BulletVisual} definition. */
public interface BulletVisualRenderer<V extends BulletVisual> {
    void render(V visual, BulletRenderContext context, PoseStack poseStack, MultiBufferSource buffer);

    ResourceLocation textureLocation(V visual, TearBullet bullet);
}
