package net.luojiuoscar.isaac_disaster.renderer.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public interface BulletVisual {
    BulletVisualTarget target();
    double priority();
    void render(BulletRenderContext context, PoseStack poseStack, MultiBufferSource buffer);
    ResourceLocation textureLocation(TearBullet bullet);
}
