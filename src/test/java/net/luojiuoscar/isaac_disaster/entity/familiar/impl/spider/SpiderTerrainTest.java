package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpiderTerrainTest {
    private static final double HALF_WIDTH = 0.0625;
    private static final double HEIGHT = 0.15;

    @Test
    void bodySweepRejectsDiagonalCornerCuttingWithClearEndpoints() {
        AABB block = new AABB(1, 0, 1, 2, 1, 2);
        Vec3 from = new Vec3(0.8, 0, 1.5);
        Vec3 to = new Vec3(1.5, 0, 0.8);
        assertFalse(SpiderTerrain.bodyIntersects(block, from, from, HALF_WIDTH, HEIGHT));
        assertFalse(SpiderTerrain.bodyIntersects(block, to, to, HALF_WIDTH, HEIGHT));
        assertTrue(SpiderTerrain.bodyIntersects(block, from, to, HALF_WIDTH, HEIGHT));
    }

    @Test
    void bodySweepAllowsFloorContactAndRejectsCeilingPenetration() {
        AABB floor = new AABB(-2, -1, -2, 2, 0, 2);
        assertFalse(SpiderTerrain.bodyIntersects(floor, Vec3.ZERO, new Vec3(1, 0, 0), HALF_WIDTH, HEIGHT));
        assertTrue(SpiderTerrain.bodyIntersects(new AABB(-1, 0.1, -1, 1, 1, 1),
                Vec3.ZERO, Vec3.ZERO, HALF_WIDTH, HEIGHT));
    }

    @Test
    void bodySweepDetectsThinObstacleBetweenEndpoints() {
        assertTrue(SpiderTerrain.bodyIntersects(new AABB(1, 0, -1, 1.01, 1, 1),
                Vec3.ZERO, new Vec3(2, 0, 0), HALF_WIDTH, HEIGHT));
        assertFalse(SpiderTerrain.bodyIntersects(new AABB(1, 0, -1, 1.01, 1, 1),
                new Vec3(0, 1, 0), new Vec3(2, 1, 0), HALF_WIDTH, HEIGHT));
    }

    @Test
    void groundQueryStaysInsideBodyColumnRegardlessOfVerticalRange() {
        AABB column = SpiderTerrain.groundColumn(new Vec3(10, 20, 30), 16, HALF_WIDTH, HEIGHT);
        assertEquals(9.9375, column.minX, 1e-9);
        assertEquals(10.0625, column.maxX, 1e-9);
        assertEquals(29.9375, column.minZ, 1e-9);
        assertEquals(30.0625, column.maxZ, 1e-9);
        assertTrue(column.minY < 4 && column.minY > 3.999);
        assertTrue(column.maxY >= 36.15 && column.maxY < 36.151);
    }

    @Test
    void placementUsesActualSlabAndEachStairTreadHeight() {
        assertEquals(new Vec3(0.5, 0.5, 0.5), ground(List.of(new AABB(0, 0, 0, 1, 0.5, 1)),
                new Vec3(0.5, 0.6, 0.5)));
        List<AABB> stair = List.of(new AABB(0, 0, 0, 1, 0.5, 1), new AABB(0.5, 0.5, 0, 1, 1, 1));
        assertEquals(new Vec3(0.25, 0.5, 0.5), ground(stair, new Vec3(0.25, 0.6, 0.5)));
        assertEquals(new Vec3(0.75, 1, 0.5), ground(stair, new Vec3(0.75, 0.6, 0.5)));
    }

    @Test
    void placementRejectsBuriedTopsAndNearbyWallsOutsideBodyFootprint() {
        List<AABB> boxes = List.of(new AABB(0, 0, 0, 1, 0.5, 1), new AABB(0, 0.55, 0, 1, 1, 1));
        assertEquals(new Vec3(0.5, 1, 0.5), ground(boxes, new Vec3(0.5, 0.5, 0.5)));
        assertNull(ground(List.of(new AABB(0.6, 0, 0, 1, 1, 1)), new Vec3(0.5, 1, 0.5)));
        assertNull(ground(List.of(), new Vec3(0.5, 1, 0.5)));
    }

    @Test
    void placementSelectsNearestClearTopWithinVerticalBounds() {
        List<AABB> boxes = List.of(new AABB(0, -2, 0, 1, -1, 1), new AABB(0, 1, 0, 1, 2, 1));
        assertEquals(new Vec3(0.5, 2, 0.5), ground(boxes, new Vec3(0.5, 1.8, 0.5)));
        AABB bounds = SpiderTerrain.groundColumn(new Vec3(0.5, 4, 0.5), 1, HALF_WIDTH, HEIGHT);
        assertNull(SpiderTerrain.groundPosition(boxes, new Vec3(0.5, 4, 0.5), bounds, HALF_WIDTH, HEIGHT));
    }

    private static Vec3 ground(List<AABB> boxes, Vec3 near) {
        return SpiderTerrain.groundPosition(boxes, near,
                SpiderTerrain.groundColumn(near, 2, HALF_WIDTH, HEIGHT), HALF_WIDTH, HEIGHT);
    }
}
