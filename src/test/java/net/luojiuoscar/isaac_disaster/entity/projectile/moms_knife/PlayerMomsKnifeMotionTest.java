package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeMotionTest {
    @Test
    void rangeUsesHalfThePanelRangeBeforeApplyingTheChargeFraction() {
        assertEquals(6, PlayerMomsKnifeMotion.range(12, 1));
        assertEquals(3, PlayerMomsKnifeMotion.range(12, 0.5));
        assertEquals(8, PlayerMomsKnifeMotion.range(64, 1));
        assertEquals(2, PlayerMomsKnifeMotion.range(64, 0.25));
        assertEquals(0, PlayerMomsKnifeMotion.range(-1, 1));
        assertEquals(8, PlayerMomsKnifeMotion.range(64, 3));
        assertEquals(0, PlayerMomsKnifeMotion.range(Double.NaN, 1));
    }

    @Test
    void flightDeceleratesToTheApexThenAcceleratesSymmetricallyBackToTheHand() {
        double range = 6;
        double speed = 1;
        double duration = PlayerMomsKnifeMotion.duration(range, speed);
        assertEquals(24, duration);
        assertEquals(0, PlayerMomsKnifeMotion.distance(range, speed, 0));
        assertEquals(range, PlayerMomsKnifeMotion.distance(range, speed, duration / 2));
        assertEquals(0, PlayerMomsKnifeMotion.distance(range, speed, duration));
        double previousStep = Double.POSITIVE_INFINITY;
        for (int tick = 1; tick <= 12; tick++) {
            double step = PlayerMomsKnifeMotion.distance(range, speed, tick)
                    - PlayerMomsKnifeMotion.distance(range, speed, tick - 1);
            assertTrue(step < previousStep);
            previousStep = step;
            assertEquals(PlayerMomsKnifeMotion.distance(range, speed, tick),
                    PlayerMomsKnifeMotion.distance(range, speed, duration - tick), 1e-10);
        }
        assertEquals(12, PlayerMomsKnifeMotion.duration(range, 2));
        assertEquals(0, PlayerMomsKnifeMotion.distance(range, speed, -1));
        assertEquals(0, PlayerMomsKnifeMotion.distance(range, speed, 100));
    }

    @Test
    void aSubTickThrowStillHasAnExactApex() {
        double range = PlayerMomsKnifeMotion.range(12, 1.0 / 28);
        double duration = PlayerMomsKnifeMotion.duration(range, 1);
        assertTrue(duration < 1);
        assertEquals(range, PlayerMomsKnifeMotion.distance(range, 1, duration / 2), 1e-10);
        assertEquals(0, PlayerMomsKnifeMotion.distance(range, 1, 1));
    }

    @Test
    void theBladeFollowsYawAndPitchIncludingBothVerticalDirections() {
        for (float yaw : new float[]{-180, -45, 0, 90, 179}) {
            for (float pitch : new float[]{-90, -40, 0, 55, 90}) {
                Vec3 direction = Vec3.directionFromRotation(pitch, yaw);
                MomKnifePose pose = PlayerMomsKnifeMotion.pose(yaw, pitch);
                assertTrue(direction.distanceTo(pose.bladePoint(Vec3.ZERO, 1)) < 1e-3);
                var blade = PlayerMomsKnifeMotion.blade(Vec3.ZERO, yaw, pitch, 6);
                assertTrue(direction.scale(6).distanceTo(blade.anchor()) < 1e-3);
                assertEquals(0.84, blade.anchor().distanceTo(blade.tip()), 1e-6);
            }
        }
    }

    @Test
    void stationaryAndFastMovingBladesHitButTheHandleAndDistantTargetsDoNot() {
        var idle = PlayerMomsKnifeMotion.blade(Vec3.ZERO, 0, 0, 0);
        assertTrue(PlayerMomsKnifeMotion.contacts(idle, idle, new AABB(-0.02, -0.02, 0.4, 0.02, 0.02, 0.5)));
        assertFalse(PlayerMomsKnifeMotion.contacts(idle, idle, new AABB(-0.01, -0.01, -0.2, 0.01, 0.01, -0.1)));
        var far = PlayerMomsKnifeMotion.blade(Vec3.ZERO, 0, 0, 8);
        assertTrue(PlayerMomsKnifeMotion.contacts(idle, far, new AABB(-0.1, -0.1, 4, 0.1, 0.1, 4.1)));
        assertFalse(PlayerMomsKnifeMotion.contacts(idle, far, new AABB(2, -0.1, 4, 3, 0.1, 4.1)));
    }
}
