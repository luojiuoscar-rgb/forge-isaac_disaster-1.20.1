package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewPlaneRandomAttackPatternTest {
    @Test
    void directionsStayUnitAndInTheViewPlaneAtEveryPitch() {
        RandomSource random = RandomSource.create(531);
        for (float pitch : new float[]{-90, -45, 0, 45, 90}) {
            Vec3 axis = Vec3.directionFromRotation(pitch, 37).normalize();
            Vec3 normal = axis.cross(GeometryHelper.lateralAxis(axis));
            boolean forward = false;
            boolean backward = false;
            for (int i = 0; i < 500; i++) {
                Vec3 direction = ViewPlaneRandomAttackPattern.randomDirection(axis, random);
                assertEquals(1, direction.lengthSqr(), 1e-12);
                assertEquals(0, direction.dot(normal), 1e-12);
                forward |= direction.dot(axis) > 0.5;
                backward |= direction.dot(axis) < -0.5;
            }
            assertTrue(forward && backward);
        }
    }

    @Test
    void tiltingTheViewTiltsTheScatterPlaneInsteadOfKeepingWorldHorizontal() {
        Vec3 horizontal = Vec3.directionFromRotation(0, 61).normalize();
        Vec3 tilted = Vec3.directionFromRotation(-45, 61).normalize();
        RandomSource random = RandomSource.create(874);
        boolean above = false;
        boolean below = false;
        for (int i = 0; i < 100; i++) {
            assertEquals(0, ViewPlaneRandomAttackPattern.randomDirection(horizontal, random).y, 1e-12);
            Vec3 direction = ViewPlaneRandomAttackPattern.randomDirection(tilted, random);
            above |= direction.y > 0.25;
            below |= direction.y < -0.25;
        }
        assertTrue(above && below);
    }
}
