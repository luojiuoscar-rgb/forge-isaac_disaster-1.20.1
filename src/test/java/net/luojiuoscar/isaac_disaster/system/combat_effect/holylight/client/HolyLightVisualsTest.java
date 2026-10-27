package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HolyLightVisualsTest {
    @Test
    void ringFollowsTheAimingPlaneEvenWhenLookingStraightUpOrDown() {
        Vec3 center = new Vec3(1, 2, 3);
        for (Vec3 axis : axes()) {
            Vec3 right = HolyLightVisuals.perpendicular(axis);
            Vec3 up = axis.cross(right).normalize();
            var capture = new Capture();
            HolyLightVisuals.drawRing(capture.consumer, center, right, up, .8, true, .5);
            assertEquals(4, capture.points.size());
            for (Vec3 point : capture.points) {
                assertEquals(0, point.subtract(center).dot(axis), .000001, "Every ring corner stays perpendicular to aim");
                assertEquals(.8 * Math.sqrt(2), point.distanceTo(center), .000001);
            }
        }
    }

    @Test
    void fullChargeKeepsTheFinalWarningAtlasFrame() {
        for (double progress : new double[]{.9999, 1, 2}) {
            var capture = new Capture();
            HolyLightVisuals.drawRing(capture.consumer, Vec3.ZERO, new Vec3(1, 0, 0),
                    new Vec3(0, 0, 1), .8, true, progress);
            assertEquals(160.5F / 256, capture.uv.get(0)[0]);
            assertEquals(.5F / 32, capture.uv.get(0)[1]);
            assertEquals(175.5F / 256, capture.uv.get(2)[0]);
        }
    }

    @Test
    void billboardWidthNeverDegeneratesForAxialOrObliqueViews() {
        for (Vec3 axis : axes()) {
            for (Vec3 cameraRelativeStart : List.of(axis.scale(3), axis.scale(-4), new Vec3(3, 2, -4), Vec3.ZERO)) {
                Vec3 side = HolyLightVisuals.billboardSide(axis, cameraRelativeStart);
                assertEquals(1, side.length(), .000001);
                assertEquals(0, axis.dot(side), .000001);
            }
        }
    }

    @Test
    void beamUsesTheFullRequestedLengthAndSoftAtlasWidthAlongAnyAxis() {
        for (Vec3 axis : axes()) {
            Vec3 start = new Vec3(2, 3, 4);
            Vec3 end = start.add(axis.scale(64));
            var capture = new Capture();
            HolyLightVisuals.drawBeam(capture.consumer, start, end, HolyLightVisuals.perpendicular(axis), .5, 10, 1);
            assertEquals(4, capture.points.size());
            assertEquals(0, capture.points.get(0).add(capture.points.get(1)).scale(.5).distanceTo(start), .00001);
            assertEquals(0, capture.points.get(2).add(capture.points.get(3)).scale(.5).distanceTo(end), .00001);
            assertEquals(1, capture.points.get(0).distanceTo(capture.points.get(1)), .00001);
            assertEquals(224.5F / 256, capture.uv.get(0)[0]);
            assertEquals(255.5F / 256, capture.uv.get(1)[0]);
            assertEquals(10.5F / 32, capture.uv.get(3)[1]);
        }
    }

    @Test
    void firingExpansionHoldAndFinishingAreSeparate() {
        assertEquals(0, HolyLightVisuals.firingRow(0, -1));
        assertTrue(HolyLightVisuals.firingRow(1, -1) < 10);
        for (double age : new double[]{2, 15, 30, 1000}) assertEquals(10, HolyLightVisuals.firingRow(age, -1));
        assertEquals(10, HolyLightVisuals.firingRow(30, 0));
        assertTrue(HolyLightVisuals.firingRow(31, 1) > 10);
        assertEquals(31, HolyLightVisuals.firingRow(32, 2));
    }

    @Test
    void cylinderHasRoundSideWallsAndBothEndFacesForEveryAimDirection() {
        Vec3 start = new Vec3(2, 3, 4);
        for (Vec3 axis : axes()) {
            Vec3 end = start.add(axis.scale(16));
            var capture = new Capture();
            HolyLightVisuals.drawCylinder(capture.consumer, start, end, axis, .5, 10, 1);
            boolean hasStartCenter = false;
            boolean hasEndCenter = false;
            boolean hasStartEdge = false;
            boolean hasEndEdge = false;
            assertTrue(capture.points.size() > 16);
            for (Vec3 point : capture.points) {
                Vec3 offset = point.subtract(start);
                double along = offset.dot(axis);
                double radial = offset.subtract(axis.scale(along)).length();
                assertTrue(Math.abs(along) < .00001 || Math.abs(along - 16) < .00001);
                assertTrue(radial < .00001 || Math.abs(radial - .5) < .00001);
                hasStartCenter |= point.distanceTo(start) < .00001;
                hasEndCenter |= point.distanceTo(end) < .00001;
                hasStartEdge |= Math.abs(along) < .00001 && radial > .49;
                hasEndEdge |= Math.abs(along - 16) < .00001 && radial > .49;
            }
            assertTrue(hasStartCenter && hasEndCenter && hasStartEdge && hasEndEdge,
                    "The beam must have a closed cylindrical core instead of a single billboard");
            assertTrue(capture.uv.stream().allMatch(uv -> Math.abs(uv[0] - 240F / 256) < .000001),
                    "The full cylinder samples the white core; its surrounding halo supplies soft edges");
        }
    }

    private static List<Vec3> axes() {
        return List.of(new Vec3(0, 1, 0), new Vec3(0, -1, 0), new Vec3(1, 0, 0),
                new Vec3(0, 0, 1), new Vec3(.3, -.5, .7).normalize());
    }

    private static final class Capture {
        private final List<Vec3> points = new ArrayList<>();
        private final List<float[]> uv = new ArrayList<>();
        private final VertexConsumer consumer = (VertexConsumer) Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),
                new Class<?>[]{VertexConsumer.class}, (proxy, method, args) -> {
                    if (method.getName().equals("vertex") && args.length == 4) {
                        points.add(new Vec3(((Number) args[1]).doubleValue(), ((Number) args[2]).doubleValue(), ((Number) args[3]).doubleValue()));
                    }
                    if (method.getName().equals("uv") && args.length == 2) uv.add(new float[]{(float) args[0], (float) args[1]});
                    return method.getReturnType() == VertexConsumer.class ? proxy : null;
                });
    }
}
