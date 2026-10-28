package net.luojiuoscar.isaac_disaster.client.renderer.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.creature.FlyModel;
import net.luojiuoscar.isaac_disaster.client.render.OwnerHostility;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.AbstractFlyFamiliarEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.scores.Team;

import java.util.UUID;

public final class FlyRenderer<T extends AbstractFlyFamiliarEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
            "textures/entity/creature/fly.png");
    private final FlyModel<T> model;

    public FlyRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new FlyModel<>(context.bakeLayer(FlyModel.LAYER_LOCATION));
        shadowRadius = 0.06F;
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        poses.pushPose();
        float visualScale = entity.getVisualScale();
        poses.scale(visualScale, visualScale, visualScale);
        poses.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poses.scale(-1.0F, -1.0F, 1.0F);
        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, 0.0F);
        var viewer = Minecraft.getInstance().player;
        var owner = entity.getOwner();
        double seconds = (Util.getNanos() % 1_000_000_000L) / 1_000_000_000.0;
        int color = renderColor(entity.getBodyColor(),
                viewer == null ? null : viewer.getUUID(), viewer == null ? null : viewer.getTeam(),
                owner == null ? null : owner.getUUID(), owner == null ? null : owner.getTeam(), seconds);
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY,
                ((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F, 1.0F);
        poses.popPose();
        super.render(entity, entityYaw, partialTick, poses, buffers, packedLight);
    }

    static int renderColor(int bodyColor, UUID viewer, Team viewerTeam,
                           UUID owner, Team ownerTeam, double seconds) {
        return color(bodyColor, shouldFlash(viewer, viewerTeam, owner, ownerTeam), seconds);
    }

    static boolean shouldFlash(UUID viewer, Team viewerTeam, UUID owner, Team ownerTeam) {
        return OwnerHostility.isHostile(viewer, viewerTeam, owner, ownerTeam);
    }

    static int color(int original, boolean flashing, double seconds) {
        // Sixteen half-cycles per second: 62.5 ms in each color, eight complete cycles.
        return flashing && (((int) Math.floor(seconds * 16)) & 1) != 0 ? 0xD8D8D8 : original;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return TEXTURE;
    }
}
