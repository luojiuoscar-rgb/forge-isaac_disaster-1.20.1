package net.luojiuoscar.isaac_disaster.renderer.layer.golden;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.renderer.layer.material.MaterialLayerSupport;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class GoldenLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/effect/golden_overlay.png");

    public GoldenLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        boolean hasGoldenVisual = EntityVisualState.hasLayer(entity, ModVisualLayers.GOLDEN.getId());
        boolean invisible = entity.isInvisible();

        if (!shouldRender(hasGoldenVisual, invisible)) {
            return;
        }

        var consumer = new MaterialLayerSupport.FaceUvVertexConsumer(
                buffer.getBuffer(RenderType.entityTranslucent(OVERLAY_TEXTURE)));
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

    static boolean shouldRender(boolean hasGoldenVisual, boolean invisible) {
        return MaterialLayerSupport.shouldRender(hasGoldenVisual, invisible);
    }
}
