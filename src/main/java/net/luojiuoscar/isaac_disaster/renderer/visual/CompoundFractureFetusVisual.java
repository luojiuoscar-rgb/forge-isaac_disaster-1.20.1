package net.luojiuoscar.isaac_disaster.renderer.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.entity.custom.FetusBullet;
import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.FetusBulletVisual;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.phys.Vec3;

/** Renders Compound Fracture fetus bullets as vanilla skeletons. */
public final class CompoundFractureFetusVisual implements BulletVisualRenderer<FetusBulletVisual> {
    private final SkeletonModel<Skeleton> model;

    public CompoundFractureFetusVisual(EntityRendererProvider.Context context) {
        model = new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON));
    }

    @Override
    public void render(FetusBulletVisual visual, BulletRenderContext context, PoseStack poseStack,
                       MultiBufferSource buffer) {
        FetusBullet bullet = (FetusBullet) context.bullet();
        poseStack.pushPose();
        Vec3 motion = bullet.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6) {
            float yaw = (float) Math.toDegrees(Math.atan2(motion.x, motion.z));
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        float scale = bullet.getScale() * 0.35F;
        poseStack.translate(0.0D, -scale, 0.0D);
        poseStack.scale(scale, scale, scale);

        int color = bullet.getColor();
        ResourceLocation texture = visual.getTexture();
        if (texture == null) {
            throw new IllegalStateException("Skeleton bullet visual requires a fixed texture");
        }
        float red = visual.acceptsTint() ? ((color >> 16) & 0xFF) / 255.0F : 1.0F;
        float green = visual.acceptsTint() ? ((color >> 8) & 0xFF) / 255.0F : 1.0F;
        float blue = visual.acceptsTint() ? (color & 0xFF) / 255.0F : 1.0F;
        model.renderToBuffer(
                poseStack,
                buffer.getBuffer(RenderType.entityTranslucent(texture)),
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
        ResourceLocation texture = visual.getTexture();
        if (texture == null) {
            throw new IllegalStateException("Skeleton bullet visual requires a fixed texture");
        }
        return texture;
    }
}
