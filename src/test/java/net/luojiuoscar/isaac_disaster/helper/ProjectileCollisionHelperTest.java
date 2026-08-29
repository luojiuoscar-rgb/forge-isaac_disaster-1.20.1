package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectileCollisionHelperTest {
    private static final double EPSILON = 1.0E-6;

    @Test
    void sweptBoundsKeepsTheProjectileBodyAcrossTheWholeMovement() {
        AABB swept = ProjectileCollisionHelper.sweptBounds(
                new AABB(-0.1, 0.0, -0.1, 0.1, 0.2, 0.1), new Vec3(4.0, -1.0, 2.0));

        assertEquals(-0.1, swept.minX, EPSILON);
        assertEquals(-1.0, swept.minY, EPSILON);
        assertEquals(4.1, swept.maxX, EPSILON);
        assertEquals(2.1, swept.maxZ, EPSILON);
    }

    @Test
    void expandedTargetDetectsOnlyTheProjectileVolume() {
        Optional<Vec3> hit = ProjectileCollisionHelper.clipExpandedTarget(
                new Vec3(0.0, 0.5, 0.0), new Vec3(10.0, 0.5, 0.0),
                new AABB(5.0, 0.0, 0.0, 6.0, 1.0, 1.0), new Vec3(0.25, 0.25, 0.25));
        assertTrue(hit.isPresent());
        assertEquals(4.75, hit.orElseThrow().x, EPSILON);

        assertFalse(ProjectileCollisionHelper.clipExpandedTarget(
                new Vec3(0.0, 0.5, 0.0), new Vec3(10.0, 0.5, 0.0),
                new AABB(5.0, 0.0, 0.26, 6.0, 1.0, 1.0), new Vec3(0.25, 0.25, 0.25)).isPresent());
    }

    @Test
    void strongestBlockedFacePrefersTheMostClippedAxis() {
        assertEquals(Direction.UP, ProjectileCollisionHelper.strongestBlockedFace(
                new Vec3(1.0, -2.0, 0.5), new Vec3(0.75, -0.2, 0.5)));
        assertEquals(Direction.DOWN, ProjectileCollisionHelper.strongestBlockedFace(
                new Vec3(1.0, 2.0, 0.5), new Vec3(0.75, 0.2, 0.5)));
        assertEquals(Direction.WEST, ProjectileCollisionHelper.strongestBlockedFace(
                new Vec3(2.0, -1.0, 0.5), new Vec3(0.1, -0.9, 0.5)));
    }
}
