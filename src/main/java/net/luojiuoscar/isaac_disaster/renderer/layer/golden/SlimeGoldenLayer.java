package net.luojiuoscar.isaac_disaster.renderer.layer.golden;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.luojiuoscar.isaac_disaster.renderer.layer.material.MaterialLayerSupport;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

/** Renders the golden surface on the slime's outer shell instead of its inner model. */
public final class SlimeGoldenLayer extends RenderLayer<Slime, SlimeModel<Slime>> {
    private static final float SLIME_OVERLAY_ALPHA = 0.7F;
    private static final ResourceLocation OVERLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/effect/golden_overlay.png");
    private final SlimeModel<Slime> model;

    public SlimeGoldenLayer(RenderLayerParent<Slime, SlimeModel<Slime>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new SlimeModel<>(modelSet.bakeLayer(ModelLayers.SLIME_OUTER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Slime entity,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!GoldenLayer.shouldRender(
                EntityVisualState.hasLayer(entity, ModVisualLayers.GOLDEN.getId()), entity.isInvisible())) {
            return;
        }

        getParentModel().copyPropertiesTo(model);
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        var consumer = new MaterialLayerSupport.FaceUvVertexConsumer(
                buffer.getBuffer(RenderType.entityTranslucent(OVERLAY_TEXTURE)));
        model.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                1.0F,
                1.0F,
                1.0F,
                SLIME_OVERLAY_ALPHA
        );
    }
}
