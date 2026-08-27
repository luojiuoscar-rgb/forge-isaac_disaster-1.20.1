package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CricketsBodySplitModulePlaneAlignmentTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void floorHitsStayTangentAfterSpinning() {
        Vec3 normal = new Vec3(0.0, 1.0, 0.0);
        Vec3 incoming = new Vec3(0.30, -0.90, 0.40);

        List<Vec3> directions = CricketsBodySplitModule.buildPlaneAlignedSpread(
                incoming, normal, 4, Math.toRadians(31.0));

        assertEquals(4, directions.size());
        directions.forEach(direction -> assertPlaneAligned(direction, normal));
        assertQuarterTurnRing(directions);
    }

    @Test
    void wallHitsStayTangentAfterSpinning() {
        Vec3 normal = new Vec3(0.0, 0.0, 1.0);
        Vec3 incoming = new Vec3(0.60, -0.20, 1.00);

        List<Vec3> directions = CricketsBodySplitModule.buildPlaneAlignedSpread(
                incoming, normal, 4, Math.toRadians(17.5));

        assertEquals(4, directions.size());
        directions.forEach(direction -> assertPlaneAligned(direction, normal));
        assertQuarterTurnRing(directions);
    }

    @Test
    void perpendicularIncomingDirectionsStillResolveToAValidPlaneBasis() {
        Vec3 normal = new Vec3(1.0, 0.0, 0.0);
        Vec3 incoming = normal.scale(2.0);

        Vec3 aligned = GeometryHelper.projectOntoPlane(incoming, normal);

        assertPlaneAligned(aligned, normal);
        assertTrue(aligned.lengthSqr() > EPSILON);
    }

    private static void assertPlaneAligned(Vec3 direction, Vec3 normal) {
        assertEquals(0.0, direction.dot(normal), EPSILON);
        assertEquals(1.0, direction.lengthSqr(), EPSILON);
    }

    private static void assertQuarterTurnRing(List<Vec3> directions) {
        assertEquals(0.0, directions.get(0).dot(directions.get(1)), EPSILON);
        assertEquals(-1.0, directions.get(0).dot(directions.get(2)), EPSILON);
        assertEquals(0.0, directions.get(1).dot(directions.get(2)), EPSILON);
        assertEquals(-1.0, directions.get(1).dot(directions.get(3)), EPSILON);
    }
}
