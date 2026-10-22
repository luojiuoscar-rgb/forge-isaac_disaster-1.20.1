package net.luojiuoscar.isaac_disaster.client.model.creature;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpiderModelTest {
    @Test
    void restGeometryAndVisibleFacesMatchTheApprovedBlockbenchProject() throws Exception {
        Map<String, List<Vertex>> rendered = capture(model());
        JsonObject source;
        try (var reader = Files.newBufferedReader(Path.of(
                "blockbench/creature/blue_spider/blue_spider.bbmodel"))) {
            source = JsonParser.parseReader(reader).getAsJsonObject();
        }
        assertEquals(source.getAsJsonArray("elements").size(), rendered.size());
        for (var value : source.getAsJsonArray("elements")) {
            JsonObject cube = value.getAsJsonObject();
            String name = cube.get("name").getAsString();
            List<Vertex> actual = rendered.get(name);
            assertNotNull(actual, name);
            float[] from = vector(cube, "from"), to = vector(cube, "to");
            float[] origin = vector(cube, "origin"), rotation = vector(cube, "rotation");
            Matrix4f transform = new Matrix4f().translate(origin[0], origin[1], origin[2])
                    .rotateZ(radians(rotation[2])).rotateY(radians(rotation[1])).rotateX(radians(rotation[0]))
                    .translate(-origin[0], -origin[1], -origin[2]);
            List<Vector3f> expected = new ArrayList<>();
            for (float x : new float[]{from[0], to[0]}) {
                for (float y : new float[]{from[1], to[1]}) {
                    for (float z : new float[]{from[2], to[2]}) {
                        expected.add(transform.transformPosition(new Vector3f(x, y, z)));
                    }
                }
            }
            // ModelPart uses downward Y and the vanilla living renderer's 24-unit origin.
            for (Vertex vertex : actual) {
                Vector3f point = new Vector3f(vertex.x * 16, 24 - vertex.y * 16, vertex.z * 16);
                assertTrue(expected.stream().anyMatch(p -> p.distance(point) < 0.0001F),
                        name + " unexpected world-space corner " + point);
            }
            for (Vector3f point : expected) {
                assertTrue(actual.stream().anyMatch(v -> new Vector3f(v.x * 16, 24 - v.y * 16, v.z * 16)
                        .distance(point) < 0.0001F), name + " missing corner " + point);
            }
            long faces = cube.getAsJsonObject("faces").entrySet().stream()
                    .filter(e -> e.getValue().getAsJsonObject().has("texture")
                            && !e.getValue().getAsJsonObject().get("texture").isJsonNull()).count();
            assertEquals(faces * 4, actual.size(), name + " visible faces, including both sides of flat fangs");
        }
    }

    @Test
    void runtimePaletteUsesNeutralBodyAndBlackLegsAndFangs() throws Exception {
        try (var stream = getClass().getResourceAsStream(
                "/assets/isaac_disaster/textures/entity/creature/spider.png")) {
            assertNotNull(stream, "Runtime texture must be packaged");
            var texture = ImageIO.read(stream);
            assertEquals(16, texture.getWidth());
            assertEquals(16, texture.getHeight());
            for (var entry : capture(model()).entrySet()) {
                var vertices = entry.getValue();
                int expected = entry.getKey().equals("body") ? 0xFFFFFFFF : 0xFF101010;
                for (int i = 0; i < vertices.size(); i += 4) {
                    float u = 0, v = 0;
                    for (int j = 0; j < 4; j++) {
                        u += vertices.get(i + j).u / 4;
                        v += vertices.get(i + j).v / 4;
                    }
                    assertEquals(expected, texture.getRGB((int) (u * 16), (int) (v * 16)),
                            entry.getKey() + " face " + i / 4);
                }
            }
        }
    }

    @Test
    void arbitraryBodyTintsLeaveLegsAndFangsUntintedInEveryPose() {
        var model=model();
        for(var clip:net.luojiuoscar.isaac_disaster.client.animation.SpiderAnimations.Clip.values()) {
            model.applyAnimation(clip,0.08F);
            var expected=capture(model).values().stream().flatMap(List::stream).toList();
            for(int color:new int[]{0x4077E0,0xD8D8D8,0xD04030,0x308050}) {
                float red=((color>>16)&255)/255F,green=((color>>8)&255)/255F,blue=(color&255)/255F;
                Capture sink=new Capture();
                model.renderToBuffer(new PoseStack(),sink,0,0,red,green,blue,1);
                assertEquals(expected.size(),sink.vertices.size(),"Every approved face is drawn exactly once");
                int bodyFaces=0;
                for(int i=0;i<sink.vertices.size();i+=4) {
                    float u=0;
                    for(int j=0;j<4;j++)u+=sink.vertices.get(i+j).u/4;
                    boolean body=u<0.5F;
                    if(body)bodyFaces++;
                    for(int j=0;j<4;j++) {
                        Vertex vertex=sink.vertices.get(i+j);
                        assertEquals(body?red:1,vertex.red,1e-6,"Only the body accepts the tint");
                        assertEquals(body?green:1,vertex.green,1e-6);
                        assertEquals(body?blue:1,vertex.blue,1e-6);
                        assertTrue(expected.stream().anyMatch(v->Math.abs(v.x-vertex.x)<1e-6
                                &&Math.abs(v.y-vertex.y)<1e-6&&Math.abs(v.z-vertex.z)<1e-6
                                &&Math.abs(v.u-vertex.u)<1e-6&&Math.abs(v.v-vertex.v)<1e-6),
                                "Separating body tint must preserve every animated vertex and UV");
                    }
                }
                assertEquals(6,bodyFaces,"The body keeps all six colored faces");
            }
        }
    }

    @Test
    void nonSpiderEntitiesRetainTheApprovedRestPose() throws Exception {
        var model = model();
        var rest = capture(model);
        for (float time : new float[]{0, 0.5F, 20, 1234}) {
            model.setupAnim(null, 3.5F, 1, time, 90, 45);
            assertEquals(rest, capture(model));
        }
    }

    private static SpiderModel<Entity> model() {
        return new SpiderModel<>(SpiderModel.createBodyLayer().bakeRoot());
    }

    private static Map<String, List<Vertex>> capture(HierarchicalModel<Entity> model) {
        Map<String, List<Vertex>> result = new LinkedHashMap<>();
        model.root().visit(new PoseStack(), (pose, path, index, cube) -> {
            String part = path.substring(path.lastIndexOf('/') + 1);
            String name = part.substring(0, part.length() - "_geometry".length());
            Capture sink = new Capture();
            cube.compile(pose, sink, 0, 0, 1, 1, 1, 1);
            result.put(name, sink.vertices);
        });
        return result;
    }

    private static float[] vector(JsonObject object, String key) {
        JsonArray a = object.getAsJsonArray(key);
        return a == null ? new float[]{0, 0, 0}
                : new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()};
    }

    private static float radians(float angle) {
        return (float) Math.toRadians(angle);
    }

    private record Vertex(float x, float y, float z, float u, float v, float red, float green, float blue) { }

    private static final class Capture implements VertexConsumer {
        private final List<Vertex> vertices = new ArrayList<>();
        @Override
        public void vertex(float x, float y, float z, float red, float green, float blue, float alpha,
                           float u, float v, int overlay, int light, float nx, float ny, float nz) {
            vertices.add(new Vertex(x, y, z, u, v, red, green, blue));
        }
        @Override public VertexConsumer vertex(double x, double y, double z) { return this; }
        @Override public VertexConsumer color(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer uv(float u, float v) { return this; }
        @Override public VertexConsumer overlayCoords(int u, int v) { return this; }
        @Override public VertexConsumer uv2(int u, int v) { return this; }
        @Override public VertexConsumer normal(float x, float y, float z) { return this; }
        @Override public void endVertex() { }
        @Override public void defaultColor(int r, int g, int b, int a) { }
        @Override public void unsetDefaultColor() { }
    }
}
