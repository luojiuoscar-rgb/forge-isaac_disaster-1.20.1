package net.luojiuoscar.isaac_disaster.renderer.layer.frozen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** Renders a tiled, translucent ice shell around an entity's collision box. */
public final class FrozenShellLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation ICE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft", "textures/block/ice.png");
    private static final float ALPHA = 0.70F;

    public FrozenShellLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!EntityVisualState.hasLayer(entity, ModVisualLayers.FROZEN.getId())) {
            return;
        }

        FrozenShellGeometry.render(
                poseStack,
                buffer.getBuffer(RenderType.entityTranslucent(ICE_TEXTURE)),
                entity,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                ALPHA
        );
    }
}
