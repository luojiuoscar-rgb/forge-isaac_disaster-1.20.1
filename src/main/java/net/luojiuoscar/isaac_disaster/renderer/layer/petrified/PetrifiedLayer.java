package net.luojiuoscar.isaac_disaster.renderer.layer.petrified;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.renderer.layer.material.MaterialLayerSupport;
import net.luojiuoscar.isaac_disaster.system.EntityVisualState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class PetrifiedLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/effect/petrified_overlay.png");

    public PetrifiedLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!MaterialLayerSupport.shouldRender(
                EntityVisualState.hasLayer(entity, ModVisualLayers.PETRIFIED.getId()), entity.isInvisible())) {
            return;
        }

        var consumer = new MaterialLayerSupport.SideRotatedFaceUvVertexConsumer(
                buffer.getBuffer(RenderType.entityCutoutNoCull(OVERLAY_TEXTURE)));
        getParentModel().renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}
