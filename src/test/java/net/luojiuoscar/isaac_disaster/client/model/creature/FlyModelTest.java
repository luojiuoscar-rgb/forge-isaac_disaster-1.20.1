package net.luojiuoscar.isaac_disaster.client.model.creature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlyModelTest {
    @Test
    void bodyTintLeavesBothWingsNeutralAcrossColorVariants() {
        for (int rgb : List.of(0x4077E0, 0xD04030, 0x308050, 0x000000)) {
            float red = ((rgb >> 16) & 255) / 255.0F;
            float green = ((rgb >> 8) & 255) / 255.0F;
            float blue = (rgb & 255) / 255.0F;
            Capture capture = new Capture();
            model().renderToBuffer(new PoseStack(), capture, 0, 0, red, green, blue, 1);
            assertEquals(24, capture.vertices.stream().filter(v ->
                    v.red == red && v.green == green && v.blue == blue).count(),
                    "Only the six body faces receive the variant color");
            assertEquals(16, capture.vertices.stream().filter(v ->
                    v.red == 1 && v.green == 1 && v.blue == 1).count(),
                    "Both sides of both wings keep the original texture color");
            assertEquals(40, capture.vertices.size());
        }
    }

    @Test
    void bodyKeepsApprovedSizeAndWingsOnlyEmitTwoFacesEach() {
        FlyModel<BlueFlyEntity> model = model();
        ModelPart fly = model.root().getChild("fly_root");
        fly.getChild("body").visit(new PoseStack(), (pose, path, index, cube) -> {
            assertEquals(2.0F, cube.maxX - cube.minX);
            assertEquals(2.0F, cube.maxY - cube.minY);
            assertEquals(2.0F, cube.maxZ - cube.minZ);
        });
        for (String name : List.of("wing_left", "wing_right")) {
            fly.getChild(name).visit(new PoseStack(), (pose, path, index, cube) -> {
                assertEquals(3.0F, cube.maxX - cube.minX);
                assertEquals(0.0F, cube.maxY - cube.minY);
                assertEquals(2.0F, cube.maxZ - cube.minZ);
                assertEquals(8, vertices(cube).size(), "Two quads per wing, without degenerate side faces");
            });
        }
        Capture capture = new Capture();
        model.renderToBuffer(new PoseStack(), capture, 0, 0, 1, 1, 1, 1);
        assertEquals(40, capture.vertices.size(), "Ten quads for the complete model");
    }

    @Test
    void wingRootsStayOpenFromEitherSideWithMirroredWings() throws Exception {
        BufferedImage texture;
        try (var source = getClass().getResourceAsStream(
                "/assets/isaac_disaster/textures/entity/creature/fly.png")) {
            assertNotNull(source);
            texture = ImageIO.read(source);
        }
        ModelPart fly = model().root().getChild("fly_root");
        for (String name : List.of("wing_left", "wing_right")) {
            fly.getChild(name).visit(new PoseStack(), (pose, path, index, cube) -> {
                List<Vertex> vertices = vertices(cube);
                for (int x = 0; x < 6; x++) {
                    for (int z = 0; z < 4; z++) {
                        float localX = (x + 0.5F) / 2.0F * (name.equals("wing_left") ? 1 : -1);
                        float localZ = -1.0F + (z + 0.5F) / 2.0F;
                        int front = sample(texture, vertices.subList(0, 4), localX, localZ);
                        int back = sample(texture, vertices.subList(4, 8), localX, localZ);
                        assertEquals(front, back, name + " physical point " + x + "," + z);
                        if (x < 2 && (z == 1 || z == 2)) {
                            assertEquals(0, front >>> 24, "Central root opening must face the body");
                        }
                        if (x == 0 && (z == 0 || z == 3)) {
                            assertEquals(255, front >>> 24, "Connection edges remain visible");
                        }
                    }
                }
            });
        }
    }

    @Test
    void wingsFlapEightTimesPerSecondAndUsePartialTicksWithoutAccumulatingPose() {
        FlyModel<BlueFlyEntity> model = model();
        ModelPart fly = model.root().getChild("fly_root");
        ModelPart left = fly.getChild("wing_left");
        ModelPart right = fly.getChild("wing_right");
        animate(model, 0.0F);
        assertEquals(Math.toRadians(-60), left.zRot, 0.00001);
        animate(model, 1.25F);
        assertEquals(Math.toRadians(30), left.zRot, 0.00001);
        assertEquals(-left.zRot, right.zRot);
        animate(model, 2.5F);
        assertEquals(Math.toRadians(-60), left.zRot, 0.00001);
        animate(model, 0.625F);
        assertEquals(Math.toRadians(-15), left.zRot, 0.00001);
        float wing = left.zRot;
        float height = fly.y;
        for (int i = 0; i < 20; i++) animate(model, 0.625F);
        assertEquals(wing, left.zRot);
        assertEquals(height, fly.y);
        animate(model, 20.625F);
        assertEquals(wing, left.zRot, 0.00001);
        assertEquals(height, fly.y, 0.00001);
        assertEquals(0.85F, left.x);
        assertEquals(-0.825F, left.y);
    }

    private static FlyModel<BlueFlyEntity> model() {
        return new FlyModel<>(FlyModel.createBodyLayer().bakeRoot());
    }

    private static void animate(FlyModel<BlueFlyEntity> model, float ticks) {
        model.setupAnim(null, 0, 0, ticks, 0, 0);
    }

    private static List<Vertex> vertices(ModelPart.Cube cube) {
        Capture capture = new Capture();
        cube.compile(new PoseStack().last(), capture, 0, 0, 1, 1, 1, 1);
        return capture.vertices;
    }

    private static int sample(BufferedImage texture, List<Vertex> quad, float x, float z) {
        Vertex a = quad.get(0), b = quad.get(1), d = quad.get(3);
        float bx = b.x - a.x, bz = b.z - a.z, dx = d.x - a.x, dz = d.z - a.z;
        float determinant = bx * dz - bz * dx;
        float s = ((x / 16 - a.x) * dz - (z / 16 - a.z) * dx) / determinant;
        float t = (bx * (z / 16 - a.z) - bz * (x / 16 - a.x)) / determinant;
        float u = a.u + s * (b.u - a.u) + t * (d.u - a.u);
        float v = a.v + s * (b.v - a.v) + t * (d.v - a.v);
        return texture.getRGB((int) (u * texture.getWidth()), (int) (v * texture.getHeight()));
    }

    private record Vertex(float x, float z, float u, float v, float red, float green, float blue) {
    }

    private static final class Capture implements VertexConsumer {
        private final List<Vertex> vertices = new ArrayList<>();

        @Override
        public void vertex(float x, float y, float z, float red, float green, float blue, float alpha,
                           float u, float v, int overlay, int light, float nx, float ny, float nz) {
            vertices.add(new Vertex(x, z, u, v, red, green, blue));
        }

        @Override public VertexConsumer vertex(double x, double y, double z) { return this; }
        @Override public VertexConsumer color(int red, int green, int blue, int alpha) { return this; }
        @Override public VertexConsumer uv(float u, float v) { return this; }
        @Override public VertexConsumer overlayCoords(int u, int v) { return this; }
        @Override public VertexConsumer uv2(int u, int v) { return this; }
        @Override public VertexConsumer normal(float x, float y, float z) { return this; }
        @Override public void endVertex() { }
        @Override public void defaultColor(int red, int green, int blue, int alpha) { }
        @Override public void unsetDefaultColor() { }
    }
}
