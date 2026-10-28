package net.luojiuoscar.isaac_disaster.client.renderer.familiar.tear_baby;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.luojiuoscar.isaac_disaster.client.model.familiar.tear_baby.TearBabyModel;
import net.luojiuoscar.isaac_disaster.entity.familiar.base.baby.AbstractTearBabyFamiliarEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** Optional renderer with Bobby/Maggy transforms by default; other rigs supply their own model and transforms. */
public class TearBabyRenderer<T extends AbstractTearBabyFamiliarEntity> extends EntityRenderer<T> {
    private final ResourceLocation texture;
    protected final EntityModel<? super T> model;

    public TearBabyRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        this(context, texture, new TearBabyModel(context.bakeLayer(TearBabyModel.LAYER_LOCATION)));
    }

    public TearBabyRenderer(EntityRendererProvider.Context context, ResourceLocation texture, EntityModel<? super T> model) {
        super(context);
        this.texture = texture;
        this.model = model;
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick,
                       @NotNull PoseStack poses, @NotNull MultiBufferSource buffers, int packedLight) {
        poses.pushPose();
        applyModelTransform(entity, partialTick, poses);
        setupModelAnimation(entity, partialTick);
        model.renderToBuffer(poses, buffers.getBuffer(model.renderType(getTextureLocation(entity))), packedLight,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        poses.popPose();
        super.render(entity, entityYaw, partialTick, poses, buffers, packedLight);
    }

    protected void applyModelTransform(T entity, float partialTick, PoseStack poses) {
        float scale = entity.getBaseModelScale() * entity.getVisualScale();
        poses.scale(scale, scale, scale);
        poses.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getRenderYaw(partialTick)));
        // The exported geometry spans Y=3..26 pixels; center it on the hover position.
        poses.translate(0, 14.5F / 16.0F, 0);
        poses.scale(-1, -1, 1);
    }

    protected void setupModelAnimation(T entity, float partialTick) {
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTick,
                entity.getRenderHeadYaw(partialTick), entity.getRenderHeadPitch(partialTick));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return texture;
    }
}
