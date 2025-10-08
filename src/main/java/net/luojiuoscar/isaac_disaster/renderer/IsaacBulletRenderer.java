package net.luojiuoscar.isaac_disaster.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.ColorHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.luojiuoscar.isaac_disaster.entity.IsaacBullet;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class IsaacBulletRenderer extends EntityRenderer<IsaacBullet> {
    private static final ResourceLocation TEAR_BULLET =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,"textures/particle/tear_bullet.png");

    public IsaacBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(IsaacBullet bullet, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        // 让粒子永远面向玩家
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        float scale = bullet.getScale();
        poseStack.scale(scale, scale, scale);

        float size = 0.15f;

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEAR_BULLET));



        // 应用滤镜
        int color = ColorHelper.blendColor(bullet.getColor(), bullet.getFilterColor(), 0.7f);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float alpha = bullet.getAlpha();


        // 绘制一个方形面片（类似粒子）
        vertexConsumer.vertex(poseStack.last().pose(), -size, -size, 0).color(r, g, b, alpha).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), size, -size, 0).color(r, g, b, alpha).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), size, size, 0).color(r, g, b, alpha).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(poseStack.last().pose(), -size, size, 0).color(r, g, b, alpha).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 1, 0).endVertex();

        poseStack.popPose();

        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(IsaacBullet entity) {
        return TEAR_BULLET;
    }
}
