package net.luojiuoscar.isaac_disaster.bullet;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SweptCollisionTest {
    @Test
    void segmentAabbReturnsEarliestPathParameter() {
        SweptCollision.OptionalDoubleHit hit = SweptCollision.segmentAabb(
                new Vec3(0, 0.5, 0), new Vec3(10, 0.5, 0),
                new AABB(5, 0, -1, 6, 1, 1));
        assertTrue(hit.hit());
        assertEquals(0.5, hit.parameter(), 1e-9);
    }

    @Test
    void segmentAabbRejectsMiss() {
        assertFalse(SweptCollision.segmentAabb(
                Vec3.ZERO, new Vec3(2, 0, 0), new AABB(3, -1, -1, 4, 1, 1)).hit());
    }

    @Test
    void detailedSweepReturnsTheOutwardFaceOfTheEntryPlane() {
        SweptCollision.SegmentHit hit = SweptCollision.segmentAabbDetailed(
                new Vec3(0, 0.5, 0), new Vec3(10, 0.5, 0),
                new AABB(5, 0, -1, 6, 1, 1));

        assertTrue(hit.hit());
        assertEquals(Direction.WEST, hit.outwardFace());
    }

    @Test
    void detailedSweepProvidesAFaceWhenStartingInsideExpandedVolume() {
        SweptCollision.SegmentHit hit = SweptCollision.segmentAabbDetailed(
                new Vec3(5.5, 0.5, 0), new Vec3(6.5, 0.5, 0),
                new AABB(5, 0, -1, 6, 1, 1));

        assertTrue(hit.hit());
        assertEquals(0.0, hit.parameter(), 1e-9);
        assertEquals(Direction.EAST, hit.outwardFace());
    }
}
