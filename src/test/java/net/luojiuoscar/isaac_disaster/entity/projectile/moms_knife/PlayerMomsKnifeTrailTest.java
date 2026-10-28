package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeTrailTest {
    @Test
    void theSpeedThresholdIsStrictAndCanBeCrossedByAnyStraightKnife() {
        var trail = new PlayerMomsKnifeTrail();
        assertFalse(trail.recordMotion(List.of(sample(0, 0), sample(1, 1.5)), Vec3.ZERO, Vec3.ZERO, 1));
        assertTrue(trail.samples().isEmpty());
        assertTrue(trail.recordMotion(List.of(sample(1, 1.5), sample(2, 3.0001)), Vec3.ZERO, Vec3.ZERO, 2));
        assertFalse(trail.samples().isEmpty());
    }

    @Test
    void trimmingInAGapDropsOldBoundsWithoutCreatingANewSegment() {
        var trail = new PlayerMomsKnifeTrail();
        trail.recordPath(List.of(sample(0, 0), sample(1, 2)), 1, true);
        trail.recordPath(List.of(sample(3, 8), sample(4, 10)), 4, true);
        var oldBounds = trail.bounds();
        trail.trim(5, 3);
        assertNull(trail.sample(2.5));
        assertNotSame(oldBounds, trail.bounds());
        assertFalse(trail.contacts(new AABB(1.9, -0.01, -0.01, 2.1, 0.01, 0.01)));
        assertTrue(trail.contacts(new AABB(8.9, -0.01, -0.01, 9.1, 0.01, 0.01)));
    }

    @Test
    void fastMotionUsesPathLengthAndIgnoresTheOwnersTranslation() {
        var trail = new PlayerMomsKnifeTrail();
        assertFalse(trail.recordMotion(List.of(sample(0, 0), sample(1, 11)), Vec3.ZERO, new Vec3(10, 0, 0), 1));
        assertTrue(trail.samples().isEmpty());
        var loop = List.of(sample(1, 0), sample(1.25, 2), sample(1.5, 4), sample(1.75, 2), sample(2, 0));
        assertTrue(trail.recordMotion(loop, Vec3.ZERO, Vec3.ZERO, 2));
        assertFalse(trail.samples().isEmpty(), "A closed path has speed even when its endpoints match");
        assertFalse(trail.recordMotion(List.of(sample(2, 0), sample(3, 1)), Vec3.ZERO, Vec3.ZERO, 3));
        assertFalse(trail.samples().isEmpty(), "Existing fast history fades rather than disappearing on slowdown");
        trail.recordMotion(List.of(sample(5, 1), sample(6, 2)), Vec3.ZERO, Vec3.ZERO, 6);
        assertTrue(trail.samples().isEmpty());
    }

    @Test
    void separateFastBurstsNeverCreateADamageBridgeAcrossSlowFrames() {
        var trail = new PlayerMomsKnifeTrail();
        trail.recordPath(List.of(sample(0, 0), sample(1, 2)), 1, true);
        trail.recordPath(List.of(sample(1, 2), sample(2, 3)), 2, false);
        trail.recordPath(List.of(sample(2, 8), sample(3, 10)), 3, true);
        assertNull(trail.sample(1.5));
        assertFalse(trail.contacts(new AABB(4.9, -0.05, -0.05, 5.1, 0.05, 0.05)));
        assertTrue(trail.contacts(new AABB(8.9, -0.05, -0.05, 9.1, 0.05, 0.05)));
        assertSame(trail.bounds(), trail.bounds(), "The aggregate search bounds are cached");
        assertEquals(2, trail.samples().stream().filter(sample -> !sample.connected()).count());
    }

    @Test
    void currentContactPathAlwaysUsesTheSharedQuarterBlockRadius() {
        var path = new PlayerMomsKnifeTrail.Path(List.of(PlayerMomsKnifeMotion.blade(Vec3.ZERO, new Vec3(0, 0, 1))));
        assertTrue(path.contacts(new AABB(0.19, -0.01, 0.4, 0.21, 0.01, 0.5)));
        assertFalse(path.contacts(new AABB(0.3, -0.01, 0.4, 0.32, 0.01, 0.5)));
    }

    @Test
    void anInstantReversalDoesNotBlendTheHistoricalBladeThroughASidewaysPose() {
        var trail = new PlayerMomsKnifeTrail();
        Vec3 before = new Vec3(1, 0, 0), after = new Vec3(-1, 0, 0);
        trail.append(List.of(new PlayerMomsKnifeTrail.Sample(10, PlayerMomsKnifeMotion.blade(Vec3.ZERO, before)),
                new PlayerMomsKnifeTrail.Sample(11, PlayerMomsKnifeMotion.blade(Vec3.ZERO, after))), 11, 3);
        var blade = trail.sample(10.999);
        assertTrue(blade.tip().subtract(blade.base()).normalize().dot(before) > 0.99999);
        blade = trail.sample(11);
        assertTrue(blade.tip().subtract(blade.base()).normalize().dot(after) > 0.99999);
    }
    private static PlayerMomsKnifeTrail.Sample sample(double time, double x) {
        return new PlayerMomsKnifeTrail.Sample(time,
                PlayerMomsKnifeMotion.blade(new Vec3(x, 0, 0), new Vec3(1, 0, 0)));
    }

    @Test
    void expiryClipsTheBoundaryAndNeverMovesHistoricalWorldPositions() {
        var trail = new PlayerMomsKnifeTrail();
        trail.append(List.of(sample(10, 0), sample(11, 2), sample(12, 4), sample(13, 6)), 13, 3);
        trail.trim(13.5, 3);
        assertEquals(10.5, trail.samples().get(0).time());
        assertEquals(new Vec3(1, 0, 0), trail.sample(10.5).anchor());
        assertEquals(new Vec3(3, 0, 0), trail.sample(11.5).anchor());
        assertNull(trail.sample(9));
        trail.trim(17, 3);
        assertTrue(trail.samples().isEmpty());
    }

    @Test
    void duplicateTickSamplesDoNotGrowTheBufferAndDisabledHistoryClearsIt() {
        var trail = new PlayerMomsKnifeTrail();
        var path = List.of(sample(1, 0), sample(2, 1));
        trail.append(path, 2, 3);
        trail.append(path, 2, 3);
        assertEquals(2, trail.samples().size());
        trail.append(List.of(sample(3, 2)), 3, 0);
        assertTrue(trail.samples().isEmpty());
    }
}
