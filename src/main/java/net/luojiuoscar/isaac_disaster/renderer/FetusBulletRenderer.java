package net.luojiuoscar.isaac_disaster.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class FetusBulletRenderer extends EntityRenderer<FetusBullet> {

    private final PlayerModel<AbstractClientPlayer> playerModel;

    public FetusBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.playerModel = new PlayerModel<>(
                context.bakeLayer(ModelLayers.PLAYER),
                false
        );
    }

    @Override
    public void render(
            FetusBullet bullet,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {

        poseStack.pushPose();

        // ================= 子弹飞行方向 =================
        Vec3 motion = bullet.getDeltaMovement();
        if (motion.lengthSqr() > 1.0e-6) {
            double dx = motion.x;
            double dy = motion.y;
            double dz = motion.z;

            // yaw：绕 Y 轴（水平）
            float yaw = (float) (Math.atan2(dx, dz) * (180 / Math.PI));

            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        // ================= 缩放 =================
        float scale = bullet.getScale() * 0.35f;
        poseStack.translate(0.0, -scale, 0.0);
        poseStack.scale(scale, scale, scale);

        // ================= 颜色 / Alpha =================
        int color = bullet.getColor();
        float alpha = bullet.getAlpha();

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // ================= 皮肤 =================
        ResourceLocation skin = getSkin(bullet);

        var vertexConsumer = buffer.getBuffer(
                RenderType.entityTranslucent(skin)
        );

        // ================= 渲染 PlayerModel =================
        playerModel.renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                r, g, b, alpha
        );

        poseStack.popPose();
        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private ResourceLocation getSkin(FetusBullet bullet) {
        if (bullet.getOwner() != null) {
            var info = Minecraft.getInstance()
                    .getConnection()
                    .getPlayerInfo(bullet.getOwner().getUUID());

            if (info != null) {
                return info.getSkinLocation();
            }
        }
        return DefaultPlayerSkin.getDefaultSkin();
    }

    @Override
    public ResourceLocation getTextureLocation(FetusBullet entity) {
        return DefaultPlayerSkin.getDefaultSkin();
    }
}
