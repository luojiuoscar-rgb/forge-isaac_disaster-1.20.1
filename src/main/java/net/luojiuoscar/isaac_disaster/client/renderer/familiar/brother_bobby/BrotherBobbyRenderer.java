package net.luojiuoscar.isaac_disaster.client.renderer.familiar.brother_bobby;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.familiar.brother_bobby.BrotherBobbyModel;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby.BrotherBobbyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class BrotherBobbyRenderer extends EntityRenderer<BrotherBobbyEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/familiar/brother_bobby.png");
    private final BrotherBobbyModel model;

    public BrotherBobbyRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new BrotherBobbyModel(context.bakeLayer(BrotherBobbyModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull BrotherBobbyEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        poses.pushPose();
        float scale = BrotherBobbyEntity.BASE_SCALE * entity.getVisualScale();
        poses.scale(scale, scale, scale);
        poses.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getRenderYaw(partialTick)));
        // The exported geometry spans Y=3..26 pixels; center it on the hover position.
        poses.translate(0, 14.5F / 16.0F, 0);
        poses.scale(-1, -1, 1);
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick,
                entity.getRenderHeadYaw(partialTick), entity.getRenderHeadPitch(partialTick));
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)), packedLight,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poses.popPose();
        super.render(entity, entityYaw, partialTick, poses, buffers, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BrotherBobbyEntity entity) {
        return TEXTURE;
    }
}
