package net.luojiuoscar.isaac_disaster.client.renderer.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.creature.FlyModel;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.AbstractFlyFamiliarEntity;
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
        poses.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poses.scale(-1.0F, -1.0F, 1.0F);
        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, 0.0F);
        int color = entity.getBodyColor();
        var viewer = Minecraft.getInstance().player;
        var owner = entity.getOwner();
        if (viewer != null && owner != null) {
            boolean flashing = shouldFlash(viewer.getUUID(), viewer.getTeam(), owner.getUUID(), owner.getTeam());
            double seconds = (Util.getNanos() % 1_000_000_000L) / 1_000_000_000.0;
            color = color(color, flashing, seconds);
        }
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY,
                ((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F,
                (color & 255) / 255.0F, 1.0F);
        poses.popPose();
        super.render(entity, entityYaw, partialTick, poses, buffers, packedLight);
    }

    static boolean shouldFlash(UUID viewer, Team viewerTeam, UUID owner, Team ownerTeam) {
        return viewer != null && owner != null && !viewer.equals(owner)
                && (viewerTeam == null || !viewerTeam.isAlliedTo(ownerTeam));
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
