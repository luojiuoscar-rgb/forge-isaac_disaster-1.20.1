package net.luojiuoscar.isaac_disaster.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.luojiuoscar.isaac_disaster.helper.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class IsaacBulletRenderer extends EntityRenderer<IsaacBullet> {
    private static final ResourceLocation TEAR_BULLET =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "textures/particle/tear_bullet.png");

    // 可调参数
    private static final float SCALE_FADE_THRESHOLD = 1.2f;    // 超过这个大小才触发淡出
    private static final float MAX_FADE_DISTANCE = 3.0f;       // 超过此距离完全不透明
    private static final int FADE_DURATION_TICKS = 10;         // 子弹生成后的持续时间

    public IsaacBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(IsaacBullet bullet, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {

        poseStack.pushPose();

        // 面向相机
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        float scale = bullet.getScale();
        poseStack.scale(scale, scale, scale);

        float size = 0.1f;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEAR_BULLET));

        // 基础颜色计算
        int color = bullet.getColor();
        int filter = bullet.getFilterColor();
        float alpha = bullet.getAlpha();

        int blended = (filter == 0xFFFFFF || filter == 0x000000)
                ? color
                : ColorHelper.blendColor(color, filter, 0.7f);

        float r = ((blended >> 16) & 0xFF) / 255f;
        float g = ((blended >> 8) & 0xFF) / 255f;
        float b = (blended & 0xFF) / 255f;


        // 近距离淡出逻辑
        Minecraft mc = Minecraft.getInstance();
        Player cameraPlayer = mc.player;


        if (cameraPlayer != null) {
            if (scale > SCALE_FADE_THRESHOLD && bullet.tickCount < FADE_DURATION_TICKS) {
                double dx = bullet.getX() - cameraPlayer.getX();
                double dy = bullet.getY() + 0.5 - cameraPlayer.getEyeY();
                double dz = bullet.getZ() - cameraPlayer.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

                // 计算距离影响因子 (0 ~ 1)
                float distFactor = (float) Math.min(1.0, dist / MAX_FADE_DISTANCE);
                // 生成时间影响因子 (线性过渡)
                float timeFactor = (float) bullet.tickCount / FADE_DURATION_TICKS;

                // 越近越透明，时间越久越不透明
                alpha = alpha * Math.min(1.0f, Math.min(1.0f, distFactor + 0.3f) * timeFactor);
            }
        }

        // 绘制方形贴图
        vertexConsumer.vertex(poseStack.last().pose(), -size, -size, 0)
                .color(r, g, b, alpha).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0, 1, 0).endVertex();

        vertexConsumer.vertex(poseStack.last().pose(), size, -size, 0)
                .color(r, g, b, alpha).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0, 1, 0).endVertex();

        vertexConsumer.vertex(poseStack.last().pose(), size, size, 0)
                .color(r, g, b, alpha).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0, 1, 0).endVertex();

        vertexConsumer.vertex(poseStack.last().pose(), -size, size, 0)
                .color(r, g, b, alpha).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0, 1, 0).endVertex();

        poseStack.popPose();

        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(IsaacBullet entity) {
        return TEAR_BULLET;
    }
}
