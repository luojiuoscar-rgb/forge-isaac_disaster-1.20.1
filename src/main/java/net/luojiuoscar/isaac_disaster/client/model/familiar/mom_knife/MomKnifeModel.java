package net.luojiuoscar.isaac_disaster.client.model.familiar.mom_knife;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

/** Approved Blockbench mesh, rendered around the handle tail with a single texture atlas. */
public final class MomKnifeModel extends EntityModel<MomKnifeEntity> {
    public static final float MODEL_SCALE = (float) (MomKnifePose.BLADE_TIP_DISTANCE / MomKnifeMesh.LENGTH);

    public MomKnifeModel() {
        super(RenderType::entityCutout);
    }

    /** Maps the authored mesh to the shared attack axis, with its edge facing pose yaw. */
    public static void applyPose(PoseStack poses, MomKnifePose pose) {
        poses.mulPose(Axis.YP.rotationDegrees(-pose.yaw()));
        poses.mulPose(Axis.XP.rotationDegrees(-pose.pitch()));
        // The authored edge is local +X; gameplay heading is local +Z.
        poses.mulPose(Axis.YP.rotationDegrees(pose.twist() - 90.0F));
        poses.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }

    @Override
    public void setupAnim(@NotNull MomKnifeEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // The entity supplies the complete rigid blade pose to the renderer.
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poses, @NotNull VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poses.last();
        for (float[] quad : MomKnifeMesh.QUADS) {
            for (int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * 5;
                buffer.vertex(pose.pose(), quad[offset], quad[offset + 1], quad[offset + 2])
                        .color(red, green, blue, alpha)
                        .uv(quad[offset + 3], quad[offset + 4])
                        .overlayCoords(packedOverlay)
                        .uv2(packedLight)
                        .normal(pose.normal(), quad[20], quad[21], quad[22])
                        .endVertex();
            }
        }
    }
}
