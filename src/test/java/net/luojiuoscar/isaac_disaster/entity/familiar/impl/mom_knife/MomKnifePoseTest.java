package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MomKnifePoseTest {
    @Test
    void neutralBladePointsDownFromTheHandleTail() {
        Vec3 anchor = new Vec3(4, 5, 6);
        MomKnifePose pose = new MomKnifePose(0, 0, 0);
        assertVector(anchor, pose.bladePoint(anchor, 0), 1e-9);
        assertVector(new Vec3(4, 4.90, 6), pose.bladePoint(anchor, MomKnifePose.BLADE_BASE_DISTANCE), 1e-9);
        assertVector(new Vec3(4, 5 - 0.84, 6),
                pose.bladePoint(anchor, MomKnifePose.BLADE_TIP_DISTANCE), 1e-9);
    }

    @Test
    void raisedBladeStaysInTheFacingPlaneAtCardinalYaws() {
        Vec3[] expected = {new Vec3(0, 0, 1), new Vec3(-1, 0, 0),
                new Vec3(0, 0, -1), new Vec3(1, 0, 0)};
        float[] yaws = {0, 90, 180, -90};
        for (int i = 0; i < yaws.length; i++) {
            for (float twist : new float[]{0, 45, 90, 137}) {
                assertVector(expected[i], new MomKnifePose(yaws[i], 90, twist).bladePoint(Vec3.ZERO, 1), 1e-9);
            }
        }
    }

    private static void assertVector(Vec3 expected, Vec3 actual, double tolerance) {
        assertEquals(expected.x, actual.x, tolerance);
        assertEquals(expected.y, actual.y, tolerance);
        assertEquals(expected.z, actual.z, tolerance);
    }
}
