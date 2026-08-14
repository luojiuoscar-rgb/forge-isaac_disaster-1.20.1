package net.luojiuoscar.isaac_disaster.renderer.layer.frozen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Emits an outward-expanded, tile-textured shell around an entity's current collision box. */
final class FrozenShellGeometry {
    private static final float EXPANSION = 1.0F / 16.0F;
    private static final float MODEL_FEET_Y = 1.501F;

    private FrozenShellGeometry() {
    }

    static void render(PoseStack poseStack, VertexConsumer consumer, LivingEntity entity,
                       int packedLight, int overlay, float alpha) {
        RenderTransform transform = createUnscaledEntityTransform(poseStack.last());
        float halfWidth = entity.getBbWidth() * 0.5F + EXPANSION;
        float top = -entity.getBbHeight() - EXPANSION;
        float bottom = EXPANSION;
        float min = -halfWidth;
        float max = halfWidth;

        renderHorizontalFaces(consumer, transform, min, max, top, bottom, packedLight, overlay, alpha);
        renderNorthAndSouthFaces(consumer, transform, min, max, top, bottom, packedLight, overlay, alpha);
        renderWestAndEastFaces(consumer, transform, min, max, top, bottom, packedLight, overlay, alpha);
    }

    /**
     * Layers run after renderer-specific scale transforms. Strip those scales while preserving
     * the renderer's camera and entity rotation so the shell follows collision-box dimensions.
     */
    private static RenderTransform createUnscaledEntityTransform(PoseStack.Pose pose) {
        Matrix4f source = pose.pose();
        Vector4f feet = source.transform(new Vector4f(0.0F, MODEL_FEET_Y, 0.0F, 1.0F));
        Vector3f xAxis = new Vector3f(source.m00(), source.m01(), source.m02()).normalize();
        Vector3f yAxis = new Vector3f(source.m10(), source.m11(), source.m12()).normalize();
        Vector3f zAxis = new Vector3f(source.m20(), source.m21(), source.m22()).normalize();
        Matrix4f positionMatrix = new Matrix4f(
                xAxis.x(), xAxis.y(), xAxis.z(), 0.0F,
                yAxis.x(), yAxis.y(), yAxis.z(), 0.0F,
                zAxis.x(), zAxis.y(), zAxis.z(), 0.0F,
                feet.x(), feet.y(), feet.z(), 1.0F
        );
        return new RenderTransform(positionMatrix, new Matrix3f(positionMatrix));
    }

    private static void renderHorizontalFaces(VertexConsumer consumer, RenderTransform transform,
                                              float min, float max, float top, float bottom,
                                              int packedLight, int overlay, float alpha) {
        for (float x0 = min; x0 < max; x0 += 1.0F) {
            float x1 = Math.min(x0 + 1.0F, max);
            float u = x1 - x0;
            for (float z0 = min; z0 < max; z0 += 1.0F) {
                float z1 = Math.min(z0 + 1.0F, max);
                float v = z1 - z0;

                emitQuad(consumer, transform,
                        x0, top, z0, x1, top, z0, x1, top, z1, x0, top, z1,
                        0.0F, -1.0F, 0.0F, u, v, packedLight, overlay, alpha);
                emitQuad(consumer, transform,
                        x0, bottom, z0, x0, bottom, z1, x1, bottom, z1, x1, bottom, z0,
                        0.0F, 1.0F, 0.0F, u, v, packedLight, overlay, alpha);
            }
        }
    }

    private static void renderNorthAndSouthFaces(VertexConsumer consumer, RenderTransform transform,
                                                  float min, float max, float top, float bottom,
                                                  int packedLight, int overlay, float alpha) {
        for (float x0 = min; x0 < max; x0 += 1.0F) {
            float x1 = Math.min(x0 + 1.0F, max);
            float u = x1 - x0;
            for (float y0 = top; y0 < bottom; y0 += 1.0F) {
                float y1 = Math.min(y0 + 1.0F, bottom);
                float v = y1 - y0;

                emitQuad(consumer, transform,
                        x0, y0, min, x0, y1, min, x1, y1, min, x1, y0, min,
                        0.0F, 0.0F, -1.0F, u, v, packedLight, overlay, alpha);
                emitQuad(consumer, transform,
                        x0, y0, max, x1, y0, max, x1, y1, max, x0, y1, max,
                        0.0F, 0.0F, 1.0F, u, v, packedLight, overlay, alpha);
            }
        }
    }

    private static void renderWestAndEastFaces(VertexConsumer consumer, RenderTransform transform,
                                                float min, float max, float top, float bottom,
                                                int packedLight, int overlay, float alpha) {
        for (float z0 = min; z0 < max; z0 += 1.0F) {
            float z1 = Math.min(z0 + 1.0F, max);
            float u = z1 - z0;
            for (float y0 = top; y0 < bottom; y0 += 1.0F) {
                float y1 = Math.min(y0 + 1.0F, bottom);
                float v = y1 - y0;

                emitQuad(consumer, transform,
                        min, y0, z0, min, y0, z1, min, y1, z1, min, y1, z0,
                        -1.0F, 0.0F, 0.0F, u, v, packedLight, overlay, alpha);
                emitQuad(consumer, transform,
                        max, y0, z0, max, y1, z0, max, y1, z1, max, y0, z1,
                        1.0F, 0.0F, 0.0F, u, v, packedLight, overlay, alpha);
            }
        }
    }

    private static void emitQuad(VertexConsumer consumer, RenderTransform transform,
                                 float x0, float y0, float z0, float x1, float y1, float z1,
                                 float x2, float y2, float z2, float x3, float y3, float z3,
                                 float normalX, float normalY, float normalZ, float maxU, float maxV,
                                 int packedLight, int overlay, float alpha) {
        emitVertex(consumer, transform, x0, y0, z0, 0.0F, 0.0F,
                normalX, normalY, normalZ, packedLight, overlay, alpha);
        emitVertex(consumer, transform, x1, y1, z1, maxU, 0.0F,
                normalX, normalY, normalZ, packedLight, overlay, alpha);
        emitVertex(consumer, transform, x2, y2, z2, maxU, maxV,
                normalX, normalY, normalZ, packedLight, overlay, alpha);
        emitVertex(consumer, transform, x3, y3, z3, 0.0F, maxV,
                normalX, normalY, normalZ, packedLight, overlay, alpha);
    }

    private static void emitVertex(VertexConsumer consumer, RenderTransform transform,
                                   float x, float y, float z, float u, float v,
                                   float normalX, float normalY, float normalZ,
                                   int packedLight, int overlay, float alpha) {
        consumer.vertex(transform.positionMatrix(), x, y, z)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(packedLight)
                .normal(transform.normalMatrix(), normalX, normalY, normalZ)
                .endVertex();
    }

    private record RenderTransform(Matrix4f positionMatrix, Matrix3f normalMatrix) {
    }
}
