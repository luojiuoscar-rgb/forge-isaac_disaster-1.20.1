package net.luojiuoscar.isaac_disaster.client.renderer.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.creature.SpiderModel;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.AbstractSpiderFamiliarEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** Renders raw familiar entities with the approved Java model's 24-unit origin. */
public final class SpiderRenderer<T extends AbstractSpiderFamiliarEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/creature/spider.png");
    private final SpiderModel<T> model;

    public SpiderRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new SpiderModel<>(context.bakeLayer(SpiderModel.LAYER_LOCATION));
        shadowRadius = 0.075F;
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        poses.pushPose();
        float visualScale = entity.getVisualScale();
        poses.scale(visualScale, visualScale, visualScale);
        applyModelTransform(poses, entity.getRenderYaw(partialTick));
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, 0, 0);
        var viewer = Minecraft.getInstance().player;
        var owner = entity.getOwner();
        double seconds = (Util.getNanos() % 1_000_000_000L) / 1_000_000_000.0;
        int color = renderColor(entity.getBodyColor(),
                viewer == null ? null : viewer.getUUID(), viewer == null ? null : viewer.getTeam(),
                owner == null ? null : owner.getUUID(), owner == null ? null : owner.getTeam(), seconds);
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)), packedLight,
                OverlayTexture.NO_OVERLAY, ((color >> 16) & 255) / 255F,
                ((color >> 8) & 255) / 255F, (color & 255) / 255F, 1);
        poses.popPose();
        super.render(entity, entityYaw, partialTick, poses, buffers, packedLight);
    }

    static void applyModelTransform(PoseStack poses, float entityYaw) {
        poses.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poses.scale(-1, -1, 1);
        // ModelPart's Y=24 is the entity's feet after the coordinate reflection.
        poses.translate(0, -1.5, 0);
    }

    static int renderColor(int bodyColor, UUID viewer, Team viewerTeam,
                           UUID owner, Team ownerTeam, double seconds) {
        boolean hostile = viewer != null && owner != null && !viewer.equals(owner)
                && (viewerTeam == null || !viewerTeam.isAlliedTo(ownerTeam));
        // Every variant alternates its own body color and gray sixteen times per second.
        return hostile && (((int) Math.floor(seconds * 16)) & 1) != 0 ? 0xD8D8D8 : bodyColor;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return TEXTURE;
    }
}
