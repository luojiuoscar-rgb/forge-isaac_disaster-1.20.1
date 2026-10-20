package net.luojiuoscar.isaac_disaster.client.renderer.familiar.mom_knife;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.familiar.mom_knife.MomKnifeModel;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Renders the Blockbench mesh around the handle-tail attack pivot. */
public final class MomKnifeRenderer extends EntityRenderer<MomKnifeEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/familiar/mom_knife.png");
    private final MomKnifeModel model;

    public MomKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MomKnifeModel();
    }

    @Override
    public void render(@NotNull MomKnifeEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        MomKnifePose pose = entity.getRenderPose(partialTick);
        poses.pushPose();
        MomKnifeModel.applyPose(poses, pose);
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, 0, 0);
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poses.popPose();
        super.render(entity, entityYaw, partialTick, poses, buffers, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MomKnifeEntity entity) {
        return TEXTURE;
    }
}
