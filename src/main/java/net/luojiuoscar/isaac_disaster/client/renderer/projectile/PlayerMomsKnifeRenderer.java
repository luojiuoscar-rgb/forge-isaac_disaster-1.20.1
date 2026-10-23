package net.luojiuoscar.isaac_disaster.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.familiar.mom_knife.MomKnifeModel;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.item.pickup.special.IsaacHead;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public final class PlayerMomsKnifeRenderer extends EntityRenderer<PlayerMomsKnifeEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/familiar/mom_knife.png");
    private final MomKnifeModel<PlayerMomsKnifeEntity> model = new MomKnifeModel<>();

    public PlayerMomsKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull PlayerMomsKnifeEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        Player owner = entity.getOwner();
        if (owner == null || !owner.isAlive() || owner.isSpectator()
                || !(owner.getMainHandItem().getItem() instanceof IsaacHead)) return;
        Vec3 anchor = PlayerMomsKnifeMotion.idlePosition(owner, partialTick)
                .add(owner.getViewVector(partialTick).scale(entity.getFlightDistance(partialTick)));
        Vec3 offset = anchor.subtract(entity.getPosition(partialTick));
        poses.pushPose();
        poses.translate(offset.x, offset.y, offset.z);
        MomKnifeModel.applyPose(poses,
                PlayerMomsKnifeMotion.pose(owner.getViewYRot(partialTick), owner.getViewXRot(partialTick)));
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)), packedLight,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poses.popPose();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PlayerMomsKnifeEntity entity) {
        return TEXTURE;
    }
}
