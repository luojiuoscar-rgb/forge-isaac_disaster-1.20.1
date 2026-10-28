package net.luojiuoscar.isaac_disaster.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.model.familiar.mom_knife.MomKnifeModel;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeTrail;
import net.luojiuoscar.isaac_disaster.item.pickup.special.IsaacHead;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class PlayerMomsKnifeRenderer extends EntityRenderer<PlayerMomsKnifeEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/familiar/mom_knife.png");
    private final MomKnifeModel<PlayerMomsKnifeEntity> model = new MomKnifeModel<>();
    private static final ResourceLocation RIBBON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");

    public PlayerMomsKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(@NotNull PlayerMomsKnifeEntity entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        Player owner = entity.getOwner();
        if (owner == null || !owner.isAlive() || owner.isSpectator()
                || !entity.discardsOnReturn() && !(owner.getMainHandItem().getItem() instanceof IsaacHead)) return;
        PlayerMomsKnifeTrail trail = entity.getRenderTrail(owner, partialTick);
        double now = entity.level().getGameTime() + partialTick;
        renderRibbon(entity, trail, now, partialTick, poses, buffers, packedLight);
        float[] opacity = {0.60F, 0.35F, 0.15F};
        for (int i = 2; i >= 0; i--) {
            var ghost = trail.sample(now - 0.75 * (i + 1));
            if (ghost != null) renderBlade(entity, ghost.anchor(), ghost.tip().subtract(ghost.base()), partialTick,
                    poses, buffers, packedLight, opacity[i]);
        }
        Vec3 anchor = entity.getRenderAnchor(owner, partialTick);
        renderBlade(entity, anchor, entity.getRenderDirection(owner, partialTick), partialTick,
                poses, buffers, packedLight, 1);
    }

    private void renderBlade(PlayerMomsKnifeEntity entity, Vec3 anchor, Vec3 direction, float partialTick,
                             PoseStack poses, MultiBufferSource buffers, int packedLight, float alpha) {
        Vec3 offset = anchor.subtract(entity.getPosition(partialTick));
        poses.pushPose();
        poses.translate(offset.x, offset.y, offset.z);
        MomKnifeModel.applyPose(poses,
                PlayerMomsKnifeMotion.pose(direction, entity.getViewYRot(partialTick)));
        model.renderToBuffer(poses, buffers.getBuffer(alpha == 1 ? model.renderType(TEXTURE) : RenderType.entityTranslucent(TEXTURE)),
                packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, alpha);
        poses.popPose();
    }

    private void renderRibbon(PlayerMomsKnifeEntity entity, PlayerMomsKnifeTrail trail, double now, float partialTick,
                              PoseStack poses, MultiBufferSource buffers, int light) {
        var samples = trail.samples();
        if (samples.size() < 2) return;
        Vec3 origin = entity.getPosition(partialTick);
        Vec3 camera = entityRenderDispatcher.camera.getPosition();
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(RIBBON_TEXTURE));
        Matrix4f matrix = poses.last().pose();
        for (int i = 1; i < samples.size(); i++) {
            var from = samples.get(i - 1); var to = samples.get(i);
            if (!to.connected()) continue;
            float tail = (float) Math.max(0, Math.min(1, (from.time() - now + 3) / 3));
            float head = (float) Math.max(0, Math.min(1, (to.time() - now + 3) / 3));
            Vec3 a = bladeCenter(from.blade()), b = bladeCenter(to.blade());
            Vec3 side = b.subtract(a).cross(camera.subtract(a));
            if (side.lengthSqr() < 1e-16 || head <= 0) continue;
            side = side.normalize().scale(0.035);
            ribbonVertex(vertices, matrix, a.subtract(side.scale(tail)).subtract(origin), 0, 0, tail, light);
            ribbonVertex(vertices, matrix, b.subtract(side.scale(head)).subtract(origin), 0, 1, head, light);
            ribbonVertex(vertices, matrix, b.add(side.scale(head)).subtract(origin), 1, 1, head, light);
            ribbonVertex(vertices, matrix, a.add(side.scale(tail)).subtract(origin), 1, 0, tail, light);
        }
    }

    private static Vec3 bladeCenter(PlayerMomsKnifeMotion.Blade blade) { return blade.base().lerp(blade.tip(), 0.5); }

    private static void ribbonVertex(VertexConsumer vertices, Matrix4f matrix, Vec3 point,
                                     float u, float v, float fade, int light) {
        vertices.vertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .color(0.78F, 0.9F, 1, 0.22F * fade).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(0, 1, 0).endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PlayerMomsKnifeEntity entity) {
        return TEXTURE;
    }
}
