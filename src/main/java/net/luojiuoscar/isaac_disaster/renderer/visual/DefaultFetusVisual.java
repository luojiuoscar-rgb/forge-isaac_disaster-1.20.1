package net.luojiuoscar.isaac_disaster.renderer.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.FetusBulletVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class DefaultFetusVisual implements BulletVisualRenderer<FetusBulletVisual> {
    private final PlayerModel<AbstractClientPlayer> widePlayerModel;
    private final PlayerModel<AbstractClientPlayer> slimPlayerModel;

    public DefaultFetusVisual(EntityRendererProvider.Context context) {
        widePlayerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        slimPlayerModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(FetusBulletVisual visual, BulletRenderContext context, PoseStack poseStack,
                       MultiBufferSource buffer) {
        FetusBullet bullet = (FetusBullet) context.bullet();
        poseStack.pushPose();
        poseStack.translate(0.0D, bullet.getCollisionHeight() * 0.5D, 0.0D);

        Vec3 motion = bullet.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6) {
            float yaw = (float) Math.toDegrees(Math.atan2(motion.x, motion.z));
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        float scale = bullet.getScale() * 0.35F;
        poseStack.translate(0.0D, -scale, 0.0D);
        poseStack.scale(scale, scale, scale);

        ResolvedPlayerAppearance appearance = resolveAppearance(bullet);
        int color = bullet.getColor();
        float red = visual.acceptsTint() ? ((color >> 16) & 0xFF) / 255.0F : 1.0F;
        float green = visual.acceptsTint() ? ((color >> 8) & 0xFF) / 255.0F : 1.0F;
        float blue = visual.acceptsTint() ? (color & 0xFF) / 255.0F : 1.0F;
        appearance.model().renderToBuffer(
                poseStack,
                buffer.getBuffer(ProjectileRenderTypes.translucent(appearance.skin())),
                context.packedLight(),
                OverlayTexture.NO_OVERLAY,
                red,
                green,
                blue,
                bullet.getAlpha()
        );
        poseStack.popPose();
    }

    @Override
    public ResourceLocation textureLocation(FetusBulletVisual visual, TearBullet bullet) {
        return resolveAppearance(bullet).skin();
    }

    private ResolvedPlayerAppearance resolveAppearance(TearBullet bullet) {
        UUID ownerUuid = bullet.getOwnerUuid();
        if (ownerUuid != null && Minecraft.getInstance().getConnection() != null) {
            PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(ownerUuid);
            if (playerInfo != null) {
                boolean slim = "slim".equals(playerInfo.getModelName());
                return new ResolvedPlayerAppearance(
                        playerInfo.getSkinLocation(),
                        slim ? slimPlayerModel : widePlayerModel);
            }
        }
        return new ResolvedPlayerAppearance(DefaultPlayerSkin.getDefaultSkin(), widePlayerModel);
    }

    private record ResolvedPlayerAppearance(
            ResourceLocation skin,
            PlayerModel<AbstractClientPlayer> model
    ) {
    }
}
