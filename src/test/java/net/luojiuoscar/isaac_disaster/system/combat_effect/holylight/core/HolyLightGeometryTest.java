package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HolyLightGeometryTest {
    @Test
    void wideTargetIntersectsWhenItsCenterIsOutsideTheCircle() {
        assertTrue(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(0.7, 0, -0.2, 2, 2, 0.2)));
        assertFalse(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(0.81, 0, -0.2, 2, 2, 0.2)));
    }

    @Test
    void circleRejectsCornersInsideOnlyTheBoundingSquare() {
        assertFalse(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(0.6, 0, 0.6, 1, 1, 1)));
        assertTrue(HolyLightGeometry.intersects(Vec3.ZERO, 1, new AABB(0.6, 0, 0.8, 1, 1, 1)));
    }

    @Test
    void verticalCylinderHitsIntersectingBodiesButDoesNotExtendAlongTheBeam() {
        assertTrue(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(-0.1, 2.4, -0.1, 0.1, 5, 0.1)));
        assertFalse(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(-0.1, 2.51, -0.1, 0.1, 5, 0.1)));
        assertTrue(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(-0.1, -2, -0.1, 0.1, -0.5, 0.1)));
        assertTrue(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(-0.1, -2, -0.1, 0.1, -1, 0.1)));
        assertFalse(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(-0.1, -2, -0.1, 0.1, -1.001, 0.1)));
        assertTrue(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(-0.1, 2.5, -0.1, 0.1, 5, 0.1)));
        assertTrue(HolyLightGeometry.intersects(Vec3.ZERO, 0.8, new AABB(0.8, 0, -0.1, 1, 2, 0.1)));
    }

    @Test
    void boundsAndNearestPointFollowWorldPosition() {
        Vec3 position = new Vec3(10, 20, 30);
        assertEquals(new AABB(9.2, 19, 29.2, 10.8, 22.5, 30.8), HolyLightGeometry.bounds(position, 0.8));
        assertEquals(new Vec3(10.4, 21, 30), HolyLightGeometry.closestPoint(
                new Vec3(10, 21, 30), new AABB(10.4, 20, 29, 12, 23, 31)));
    }

    @Test
    void tangentIntersectionIsStableAfterWorldCoordinateTranslation() {
        assertTrue(HolyLightGeometry.intersects(new Vec3(240.5, 20, 30.5), 0.4,
                new AABB(240.9, 20, 30.4, 242, 22, 30.6)));
        assertFalse(HolyLightGeometry.intersects(new Vec3(240.5, 20, 30.5), 0.4,
                new AABB(240.90001, 20, 30.4, 242, 22, 30.6)));
    }
}
