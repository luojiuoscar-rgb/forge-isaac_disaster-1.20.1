package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlyOrbitLayoutTest {
    @Test
    void aFullRingKeepsBodyAndWingSpacingAtAllSupportedOwnerSizes() {
        for (double width : new double[]{0.15, 0.6, 1.2}) {
            for (int count : new int[]{1, 64, 128}) {
                double radius = FlyOrbitLayout.radius(width, count);
                assertTrue(radius >= width / 2 + 0.5);
                if (count > 1) {
                    double innerGap = 2 * (radius - 0.25) * Math.tan(Math.PI / count);
                    assertTrue(innerGap >= 0.175 - 1e-9, "Wing roots must not crowd adjacent tangential footprints");
                }
                Vec3 center = new Vec3(10, 3, -4);
                for (int slot = 0; slot < count; slot++) {
                    Vec3 position = FlyOrbitLayout.position(center, radius, 0.125, slot, count, 0.7);
                    assertEquals(center.y, position.y + 0.125 / 2, 1e-9);
                    assertEquals(radius, position.subtract(center).multiply(1, 0, 1).length(), 1e-9);
                    if (slot > 0) assertNotEquals(
                            FlyOrbitLayout.position(center, radius, 0.125, slot - 1, count, 0.7), position);
                }
            }
        }
        assertEquals(2.1, FlyOrbitLayout.radius(0.6, 64), 1e-6);
        assertTrue(FlyOrbitLayout.radius(1.2, 64) > FlyOrbitLayout.radius(0.6, 64));
        assertTrue(FlyOrbitLayout.radius(0.6, 128) > FlyOrbitLayout.radius(0.6, 64));
    }

    @Test
    void orbitAdvancesByTheSameWorldSpeedForDifferentRadii() {
        for (double radius : new double[]{2.1, 4.2, 12}) {
            double next = FlyOrbitLayout.advance(0.5, radius);
            Vec3 a = FlyOrbitLayout.position(Vec3.ZERO, radius, 0.125, 0, 64, 0.5);
            Vec3 b = FlyOrbitLayout.position(Vec3.ZERO, radius, 0.125, 0, 64, next);
            assertEquals(0.3, a.distanceTo(b), 1e-9);
        }
    }
}
