package net.luojiuoscar.isaac_disaster.renderer.layer.golden;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.system.EntityVisualState;
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

        VertexConsumer consumer = new FaceUvVertexConsumer(
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
        return hasGoldenVisual && !invisible;
    }

    static final class FaceUvVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private int vertexInFace;

        FaceUvVertexConsumer(VertexConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            switch (vertexInFace) {
                case 0 -> delegate.uv(0.0F, 0.0F);
                case 1 -> delegate.uv(0.0F, 1.0F);
                case 2 -> delegate.uv(1.0F, 1.0F);
                default -> delegate.uv(1.0F, 0.0F);
            }
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
            vertexInFace = (vertexInFace + 1) % 4;
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, alpha);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
