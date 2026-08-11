package net.luojiuoscar.isaac_disaster.renderer.layer.material;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Shared helpers for entity material overlay layers.
 *
 * <p>The vertex consumers remap each emitted model face to the complete
 * overlay texture while forwarding all other vertex data to the original
 * consumer. They do not modify the entity model or other renderer layers.</p>
 */
public final class MaterialLayerSupport {
    private MaterialLayerSupport() {
    }

    public static boolean shouldRender(boolean hasVisual, boolean invisible) {
        return hasVisual && !invisible;
    }

    /** Replaces model skin UVs with a complete texture mapping for each face. */
    public static final class FaceUvVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private int vertexInFace;

        public FaceUvVertexConsumer(VertexConsumer delegate) {
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
            delegate.uv(faceU(vertexInFace), faceV(vertexInFace));
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

        private static float faceU(int vertex) {
            return vertex == 0 || vertex == 1 ? 0.0F : 1.0F;
        }

        private static float faceV(int vertex) {
            return vertex == 0 || vertex == 3 ? 0.0F : 1.0F;
        }
    }

    /**
     * Applies a fixed 90-degree UV rotation to the four side faces of each
     * standard six-face model cube. The mapping uses only emitted face order,
     * so it cannot change with the camera or rendering matrix.
     */
    public static final class SideRotatedFaceUvVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private int vertexInFace;
        private int faceInCube;

        public SideRotatedFaceUvVertexConsumer(VertexConsumer delegate) {
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
            if (faceInCube >= 2) {
                delegate.uv(sideU(vertexInFace), sideV(vertexInFace));
            } else {
                delegate.uv(FaceUvVertexConsumer.faceU(vertexInFace),
                        FaceUvVertexConsumer.faceV(vertexInFace));
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
            vertexInFace++;
            if (vertexInFace == 4) {
                vertexInFace = 0;
                faceInCube = (faceInCube + 1) % 6;
            }
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, alpha);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }

        private static float sideU(int vertex) {
            return vertex == 0 || vertex == 3 ? 1.0F : 0.0F;
        }

        private static float sideV(int vertex) {
            return vertex == 0 || vertex == 1 ? 0.0F : 1.0F;
        }
    }
}
