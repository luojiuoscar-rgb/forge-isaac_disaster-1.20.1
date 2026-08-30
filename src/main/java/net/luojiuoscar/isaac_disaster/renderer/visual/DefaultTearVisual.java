package net.luojiuoscar.isaac_disaster.renderer.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.TearBulletVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class DefaultTearVisual implements BulletVisualRenderer<TearBulletVisual> {
    @Override
    public ResourceLocation textureLocation(TearBulletVisual visual, TearBullet bullet) {
        return visual.getTexture();
    }

    @Override
    public void render(TearBulletVisual visual, BulletRenderContext context, PoseStack poseStack,
                       MultiBufferSource buffer) {
        TearBullet bullet = context.bullet();
        poseStack.pushPose();
        poseStack.translate(0.0D, bullet.getCollisionHeight() * 0.5D, 0.0D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        float scale = bullet.getScale();
        poseStack.scale(scale, scale, scale);

        float alpha = bullet.getAlpha();
        Player camera = Minecraft.getInstance().player;
        if (camera != null && scale > 1.4F && bullet.tickCount < 6) {
            double dx = bullet.getX() - camera.getX();
            double dy = bullet.getY() + 0.5D - camera.getEyeY();
            double dz = bullet.getZ() - camera.getZ();
            float distanceFactor = (float) Math.min(
                    1.0D,
                    Math.sqrt(dx * dx + dy * dy + dz * dz) / 2.0D);
            float birthFactor = Math.min(
                    1.0F,
                    (bullet.tickCount + context.partialTicks()) / 6.0F);
            alpha *= Math.min(1.0F, (distanceFactor + 0.3F) * birthFactor);
        }

        int color = bullet.getColor();
        float red = visual.acceptsTint() ? ((color >> 16) & 255) / 255.0F : 1.0F;
        float green = visual.acceptsTint() ? ((color >> 8) & 255) / 255.0F : 1.0F;
        float blue = visual.acceptsTint() ? (color & 255) / 255.0F : 1.0F;
        alpha = Math.max(0.0F, Math.min(1.0F, alpha));

        VertexConsumer vertexConsumer = buffer.getBuffer(ProjectileRenderTypes.translucent(visual.getTexture()));
        float halfSize = 0.1F;
        quad(vertexConsumer, poseStack, -halfSize, -halfSize, 0.0F, 0.0F, 1.0F,
                red, green, blue, alpha, context.packedLight());
        quad(vertexConsumer, poseStack, halfSize, -halfSize, 0.0F, 1.0F, 1.0F,
                red, green, blue, alpha, context.packedLight());
        quad(vertexConsumer, poseStack, halfSize, halfSize, 0.0F, 1.0F, 0.0F,
                red, green, blue, alpha, context.packedLight());
        quad(vertexConsumer, poseStack, -halfSize, halfSize, 0.0F, 0.0F, 0.0F,
                red, green, blue, alpha, context.packedLight());
        poseStack.popPose();
    }

    private static void quad(VertexConsumer vertexConsumer, PoseStack poseStack,
                             float x, float y, float z, float u, float v,
                             float red, float green, float blue, float alpha,
                             int packedLight) {
        vertexConsumer.vertex(poseStack.last().pose(), x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
