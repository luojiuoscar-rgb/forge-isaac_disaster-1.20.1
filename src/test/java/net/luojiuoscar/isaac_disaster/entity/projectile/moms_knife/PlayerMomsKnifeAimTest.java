package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeAimTest {
    @Test
    void firstIntersectionWinsRegardlessOfCandidateOrderOrBodyCenter() {
        Vec3 start = Vec3.ZERO;
        Vec3 end = new Vec3(0, 0, 24);
        AABB nearSurfaceFarCenter = new AABB(-1, -1, 4, 1, 1, 20);
        AABB fartherSurfaceNearCenter = new AABB(-1, -1, 6, 1, 1, 7);
        assertEquals(new Vec3(0, 0, 4), PlayerMomsKnifeAim.firstHit(start, end,
                List.of(fartherSurfaceNearCenter, nearSurfaceFarCenter)).orElseThrow());
    }

    @Test
    void offRayBehindAndBeyondRangeTargetsDoNotCountAsHits() {
        assertTrue(PlayerMomsKnifeAim.firstHit(Vec3.ZERO, new Vec3(0, 0, 24), List.of(
                new AABB(1, -1, 4, 2, 1, 6),
                new AABB(-1, -1, -3, 1, 1, -1),
                new AABB(-1, -1, 24.1, 1, 1, 26)
        )).isEmpty());
        assertEquals(new Vec3(0, 0, 23.9), PlayerMomsKnifeAim.firstHit(Vec3.ZERO, new Vec3(0, 0, 24),
                List.of(new AABB(-1, -1, 23.9, 1, 1, 26))).orElseThrow());
    }

    @Test
    void anEyeAlreadyInsideATargetUsesTheEyePoint() {
        assertEquals(Vec3.ZERO, PlayerMomsKnifeAim.firstHit(Vec3.ZERO, new Vec3(0, 0, 24),
                List.of(new AABB(-1, -1, -1, 1, 1, 1))).orElseThrow());
    }

    @Test
    void theRaySupportsBothVerticalDirections() {
        AABB above = new AABB(-1, 4, -1, 1, 6, 1);
        AABB below = new AABB(-1, -6, -1, 1, -4, 1);
        assertEquals(new Vec3(0, 4, 0), PlayerMomsKnifeAim.firstHit(Vec3.ZERO, new Vec3(0, 24, 0),
                List.of(below, above)).orElseThrow());
        assertEquals(new Vec3(0, -4, 0), PlayerMomsKnifeAim.firstHit(Vec3.ZERO, new Vec3(0, -24, 0),
                List.of(above, below)).orElseThrow());
    }
}
