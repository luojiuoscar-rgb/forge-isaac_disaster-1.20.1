package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeMotionTest {
    @Test
    void entityPaddingCatchesGrazingTargetsWithoutFillingTheCircleOrWideningBlocks() {
        var blade = PlayerMomsKnifeMotion.blade(new Vec3(3, 0, 0), new Vec3(0, 0, 1));
        var grazing = new AABB(3.19, -0.01, 0.4, 3.21, 0.01, 0.5);
        assertFalse(PlayerMomsKnifeMotion.contacts(blade, blade, grazing));
        assertTrue(PlayerMomsKnifeMotion.contacts(blade, blade, grazing, 0.17));
        assertNull(PlayerMomsKnifeBlockContacts.contact(blade, blade, grazing));
        assertFalse(PlayerMomsKnifeMotion.contacts(blade, blade,
                new AABB(3.3, -0.01, 0.4, 3.32, 0.01, 0.5), 0.17));
        assertFalse(PlayerMomsKnifeMotion.contacts(blade, blade,
                new AABB(1.4, -0.01, 0.4, 1.6, 0.01, 0.5), 0.17));
        assertEquals(0.08, blade.radius());
    }

    @Test
    void paddedSweepHitsFastGrazesInAnyPlaneWithoutChangingItsPath() {
        for (float yaw : new float[]{0, 65, 170}) for (float pitch : new float[]{-90, 0, 90}) {
            Vec3 axis = PlayerMomsKnifeMotion.toWorld(new Vec3(0, 0, 1), yaw, pitch);
            Vec3 right = PlayerMomsKnifeMotion.toWorld(new Vec3(1, 0, 0), yaw, pitch);
            Vec3 normal = axis.cross(right).normalize();
            var from = PlayerMomsKnifeMotion.blade(Vec3.ZERO, axis);
            var to = PlayerMomsKnifeMotion.blade(right.scale(8), axis);
            Vec3 center = right.scale(4).add(axis.scale(0.5)).add(normal.scale(0.2));
            AABB target = new AABB(center, center).inflate(0.005);
            assertFalse(PlayerMomsKnifeMotion.contacts(from, to, target));
            assertTrue(PlayerMomsKnifeMotion.contacts(from, to, target, 0.17));
            assertEquals(right.scale(8), to.anchor());
        }
    }

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

    @Test
    void oneWayMovementClipsOnlyTheFinalStep() {
        double remaining = 10;
        double traveled = 0;
        for (double expected : new double[]{3, 3, 3, 1}) {
            double step = PlayerMomsKnifeMotion.oneWayStep(remaining, 3);
            assertEquals(expected, step);
            remaining -= step;
            traveled += step;
        }
        assertEquals(10, traveled);
        assertEquals(0, PlayerMomsKnifeMotion.oneWayStep(remaining, 3));
        assertEquals(0, PlayerMomsKnifeMotion.oneWayStep(10, Double.NaN));
    }

    @Test
    void steeringPreservesRadialDistanceAndOneWaySpeedWhenFollowingACurvedPath() {
        Vec3 direction = PlayerMomsKnifeMotion.FORWARD;
        Vec3 anchor = new Vec3(10, 20, 30);
        Vec3 position = anchor;
        double remaining = 10;
        double total = 0;
        for (int tick = 1; tick <= 24; tick++) {
            direction = PlayerMomsKnifeMotion.steer(direction, new Vec3(Math.sin(tick), 0.3, Math.cos(tick)));
            assertEquals(1, direction.length(), 1e-10);
            double radius = PlayerMomsKnifeMotion.distance(6, 1, tick);
            assertEquals(radius, anchor.add(direction.scale(radius)).distanceTo(anchor), 1e-10);
            double step = PlayerMomsKnifeMotion.oneWayStep(remaining, 3);
            Vec3 next = position.add(direction.scale(step));
            assertEquals(step, next.distanceTo(position), 1e-10);
            remaining -= step;
            total += step;
            position = next;
        }
        assertEquals(10, total);
        assertEquals(0, remaining);
        assertEquals(0, PlayerMomsKnifeMotion.distance(6, 1, 24));
    }

    @Test
    void localDeflectionFollowsTurningAndIsStableAtVerticalViews() {
        Vec3 local = new Vec3(0.2, 0.3, 0.9).normalize();
        for (float yaw : new float[]{-180, -45, 0, 90, 179}) {
            for (float pitch : new float[]{-90, -89, 0, 89, 90}) {
                Vec3 direction = PlayerMomsKnifeMotion.toWorld(local, yaw, pitch);
                assertEquals(1, direction.length(), 1e-8);
                assertTrue(local.distanceTo(PlayerMomsKnifeMotion.toLocal(direction, yaw, pitch)) < 1e-6);
                Vec3 forward = PlayerMomsKnifeMotion.toWorld(PlayerMomsKnifeMotion.FORWARD, yaw, pitch);
                Vec3 vanilla = Vec3.directionFromRotation(pitch, yaw).normalize();
                assertTrue(forward.distanceTo(vanilla) < 1e-6,
                        () -> "yaw=" + yaw + ", pitch=" + pitch + ", vanillaLength=" + vanilla.length()
                                + ", directionError=" + forward.distanceTo(vanilla));
                MomKnifePose pose = PlayerMomsKnifeMotion.pose(direction, yaw);
                assertTrue(direction.distanceTo(pose.bladePoint(Vec3.ZERO, 1)) < 1e-6);
            }
        }
    }

    @Test
    void oppositeDirectionsAndDegenerateTargetsNeverCollapseTheBlade() {
        Vec3 from = PlayerMomsKnifeMotion.FORWARD;
        Vec3 to = from.scale(-1);
        assertTrue(PlayerMomsKnifeMotion.interpolateDirection(from, to, 1).distanceTo(to) < 1e-10);
        for (double fraction : new double[]{0, 0.25, 0.5, 0.75, 1}) {
            Vec3 direction = PlayerMomsKnifeMotion.interpolateDirection(from, to, fraction);
            var blade = PlayerMomsKnifeMotion.blade(Vec3.ZERO, direction);
            assertEquals(1, direction.length(), 1e-10);
            assertEquals(0.84, blade.tip().length(), 1e-10);
        }
        assertEquals(from, PlayerMomsKnifeMotion.steer(from, Vec3.ZERO));
        assertEquals(from, PlayerMomsKnifeMotion.steer(from, new Vec3(Double.NaN, 0, 0)));
    }

    @Test
    void controlMaintainsSpeedAndTractorBeamIncreasesTheTurningBudget() {
        Vec3 velocity = new Vec3(0, 0, 1.7);
        Vec3 target = new Vec3(64, 0, 0);
        Vec3 normal = PlayerMomsKnifeMotion.controlVelocity(Vec3.ZERO, velocity, target, false);
        Vec3 boosted = PlayerMomsKnifeMotion.controlVelocity(Vec3.ZERO, velocity, target, true);
        assertEquals(1.7, normal.length(), 1e-10);
        assertEquals(1.7, boosted.length(), 1e-10);
        assertTrue(normal.x > 0 && boosted.x > normal.x);
        assertTrue(boosted.z < normal.z);
    }

    @Test
    void reverseVerticalAndDegenerateControlNeverStallTheKnife() {
        Vec3 velocity = new Vec3(0, 0, 1.25);
        for (Vec3 target : new Vec3[]{new Vec3(0, 0, -64), new Vec3(0, 64, 0), new Vec3(0, -64, 0)}) {
            Vec3 result = PlayerMomsKnifeMotion.controlVelocity(Vec3.ZERO, velocity, target, true);
            assertEquals(1.25, result.length(), 1e-10);
        }
        assertEquals(velocity, PlayerMomsKnifeMotion.controlVelocity(Vec3.ZERO, velocity, Vec3.ZERO, true));
        assertEquals(Vec3.ZERO, PlayerMomsKnifeMotion.controlVelocity(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, false));
        Vec3 small = new Vec3(0, 0, 0.1);
        assertEquals(small, PlayerMomsKnifeMotion.controlVelocity(Vec3.ZERO, small, new Vec3(0, 0, -0.625), true));
    }

    @Test
    void squareScatterKeepsIndependentOffsetsAndNeverMovesAlongTheViewAxis() {
        Vec3 center = new Vec3(10, 20, 30);
        for (float yaw : new float[]{-179, 0, 45, 90}) {
            for (float pitch : new float[]{-90, 0, 90}) {
                Vec3 right = PlayerMomsKnifeMotion.toWorld(new Vec3(1, 0, 0), yaw, pitch);
                Vec3 up = PlayerMomsKnifeMotion.toWorld(new Vec3(0, 1, 0), yaw, pitch);
                Vec3 forward = PlayerMomsKnifeMotion.toWorld(PlayerMomsKnifeMotion.FORWARD, yaw, pitch);
                for (double x : new double[]{-1, 0, 1}) {
                    for (double y : new double[]{-1, 0, 1}) {
                        Vec3 offset = PlayerMomsKnifeMotion.scatterOrigin(center, yaw, pitch, x, y).subtract(center);
                        assertEquals(x, offset.dot(right), 1e-7);
                        assertEquals(y, offset.dot(up), 1e-7);
                        assertEquals(0, offset.dot(forward), 1e-7);
                    }
                }
            }
        }
        assertEquals(center, PlayerMomsKnifeMotion.scatterOrigin(center, 0, 0, Double.NaN, Double.POSITIVE_INFINITY));
    }
}
