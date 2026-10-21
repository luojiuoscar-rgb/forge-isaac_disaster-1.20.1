package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class MomKnifeAttackTest {
    @ParameterizedTest
    @ValueSource(ints = {2, 4, 6})
    void aimUsesItsSampledDurationAndStillTracksTheLatestTarget(int duration) {
        Vec3 anchor = new Vec3(2, 5, 3);
        MomKnifePose start = new MomKnifePose(179, 0, 0);
        Vec3 target = anchor.add(1, -1, 0);
        assertEquals(start, MomKnifeAttack.aim(anchor, target, start, 0, duration));
        MomKnifePose halfway = MomKnifeAttack.aim(anchor, target, start, duration / 2.0, duration);
        assertEquals(134.5, halfway.yaw(), 1e-5);
        assertEquals(-22.5, halfway.pitch(), 1e-5);
        MomKnifePose finished = MomKnifeAttack.aim(anchor, target, start, duration, duration);
        assertVector(target.subtract(anchor).normalize(), finished.bladePoint(anchor, 1).subtract(anchor));
        Vec3 moved = anchor.add(-2, 3, -4);
        MomKnifePose tracked = MomKnifeAttack.aim(anchor, moved, start, duration + 1, duration);
        assertVector(moved.subtract(anchor).normalize(), tracked.bladePoint(anchor, 1).subtract(anchor));
        assertEquals(anchor, tracked.bladePoint(anchor, 0));
    }

    @Test
    void aimClampsNonPositiveDurationsToOneTick() {
        MomKnifePose start = new MomKnifePose(0, 0, 0);
        for (int duration : new int[]{0, -3}) {
            assertEquals(start, MomKnifeAttack.aim(Vec3.ZERO, new Vec3(0, 0, 1), start, 0, duration));
            MomKnifePose finished = MomKnifeAttack.aim(Vec3.ZERO, new Vec3(0, 0, 1), start, 1, duration);
            assertVector(new Vec3(0, 0, 1), finished.bladePoint(Vec3.ZERO, 1));
        }
    }

    @Test
    void riseLocksHorizontalPositionAndFinishesInThreeTicks() {
        Vec3 origin = new Vec3(4, 2, -7);
        assertEquals(origin, MomKnifeAttack.rise(origin, 1.5, 0));
        assertEquals(new Vec3(4, 2.5, -7), MomKnifeAttack.rise(origin, 1.5, 1));
        assertEquals(new Vec3(4, 3.5, -7), MomKnifeAttack.rise(origin, 1.5, 3));
        assertEquals(new Vec3(4, 3.5, -7), MomKnifeAttack.rise(origin, 1.5, 20));
    }

    @Test
    void randomHeightAndCooldownIncludeTheRequestedBounds() {
        assertEquals(0.5, MomKnifeAttack.riseHeight(0));
        assertEquals(1.25, MomKnifeAttack.riseHeight(0.5));
        assertEquals(2, MomKnifeAttack.riseHeight(1));
        assertEquals(20, MomKnifeAttack.MIN_COOLDOWN);
        assertEquals(30, MomKnifeAttack.MAX_COOLDOWN);
        assertTrue(MomKnifeAttack.RETURN_SPEED < MomKnifeAttack.THRUST_SPEED / 4);
    }

    @Test
    void aimingRotatesAroundTheHandleAndTracksTheLatestTarget() {
        Vec3 anchor = new Vec3(2, 5, 3);
        MomKnifePose start = new MomKnifePose(179, 0, 0);
        Vec3 target = anchor.add(1, -1, 0);
        assertEquals(start, MomKnifeAttack.aim(anchor, target, start, 0, 3));
        MomKnifePose middle = MomKnifeAttack.aim(anchor, target, start, 1.5, 3);
        assertEquals(134.5, middle.yaw(), 1e-5);
        assertEquals(-22.5, middle.pitch(), 1e-5);
        MomKnifePose aimed = MomKnifeAttack.aim(anchor, target, start, 3, 3);
        assertVector(target.subtract(anchor).normalize(), aimed.bladePoint(anchor, 1).subtract(anchor));
        Vec3 moved = anchor.add(-2, 3, -4);
        MomKnifePose changed = MomKnifeAttack.aim(anchor, moved, start, 4, 3);
        assertVector(moved.subtract(anchor).normalize(), changed.bladePoint(anchor, 1).subtract(anchor));
        assertEquals(anchor, changed.bladePoint(anchor, 0));
        assertEquals(0, changed.twist());
    }

    @Test
    void aimHandlesTargetsDirectlyAboveBelowAndAtTheHandle() {
        MomKnifePose fallback = new MomKnifePose(37, 0, 0);
        assertEquals(-180, MomKnifeAttack.aim(Vec3.ZERO, new Vec3(0, 1, 0), fallback, 3, 3).pitch());
        assertEquals(0, MomKnifeAttack.aim(Vec3.ZERO, new Vec3(0, -1, 0), fallback, 3, 3).pitch(), 1e-6);
        assertEquals(37, MomKnifeAttack.aim(Vec3.ZERO, new Vec3(0, 1, 0), fallback, 3, 3).yaw(), 1e-6);
        assertEquals(37, MomKnifeAttack.aim(Vec3.ZERO, new Vec3(0, -1, 0), fallback, 3, 3).yaw(), 1e-6);
        assertEquals(fallback, MomKnifeAttack.aim(Vec3.ZERO, Vec3.ZERO, fallback, 3, 3));
    }

    @Test
    void thrustClosesDistanceAtTheConfiguredSpeedWithoutAnEarlyHit() {
        AABB target = new AABB(-0.5, -0.5, 5, 0.5, 0.5, 6);
        Step step = thrust(Vec3.ZERO, target);
        assertFalse(step.hit());
        assertEquals(MomKnifeAttack.THRUST_SPEED, step.position().length(), 1e-8);
        assertVector(new Vec3(0, 0, 1), step.pose().bladePoint(Vec3.ZERO, 1));
    }

    @Test
    void thrustDetectsTipContactAndContinuesThroughTheVictim() {
        AABB target = new AABB(-0.3, -0.3, 2, 0.3, 0.3, 2.1);
        Step step = thrust(Vec3.ZERO, target);
        assertTrue(step.hit());
        assertEquals(MomKnifeAttack.THRUST_SPEED, step.position().z, 1e-7);
        assertEquals(MomKnifeAttack.THRUST_SPEED + MomKnifePose.BLADE_TIP_DISTANCE,
                step.pose().bladePoint(step.position(), MomKnifePose.BLADE_TIP_DISTANCE).z, 1e-7);
        assertTrue(step.position().z < target.minZ);
    }

    @Test
    void thrustKeepsItsLaunchDirectionAndStillDetectsThinTargetsBetweenTicks() {
        AABB first = new AABB(-0.1, -0.1, 5, 0.1, 0.1, 5.01);
        Step step = thrust(Vec3.ZERO, first);
        AABB moved = new AABB(3, -0.1, 3, 3.01, 0.1, 3.01);
        Step straight = thrust(step.position(), moved);
        assertEquals(step.position().x, straight.position().x, 1e-8);
        assertFalse(straight.hit());
        assertVector(new Vec3(0, 0, 1), straight.pose().bladePoint(Vec3.ZERO, 1));
        AABB thin = new AABB(-0.001, -0.001, 1.4, 0.001, 0.001, 1.401);
        assertTrue(thrust(Vec3.ZERO, thin).hit());
    }

    @Test
    void contactAlreadyInsideTheBladeStillAdvancesAtThrustSpeed() {
        AABB target = new AABB(-0.3, -0.3, 0.5, 0.3, 0.3, 0.6);
        Step contact = thrust(Vec3.ZERO, target);
        assertTrue(contact.hit());
        assertEquals(MomKnifeAttack.THRUST_SPEED, contact.position().z, 1e-8);
    }

    @Test
    void returnRecoversFromItsCurrentPoseToNeutral() {
        assertEquals(new MomKnifePose(37, 123, 180), MomKnifeAttack.returnPose(37, new MomKnifePose(37, 123, 180), 0));
        assertEquals(new MomKnifePose(37, 61.5f, 90), MomKnifeAttack.returnPose(37, new MomKnifePose(37, 123, 180), 4));
        assertEquals(new MomKnifePose(37, 0, 0), MomKnifeAttack.returnPose(37, new MomKnifePose(37, 123, 180), 8));
    }

    @Test
    void finalFlightStepRespectsTheRemainingRange() {
        MomKnifePose launch = new MomKnifePose(37, 123, 0);
        Vec3 finalPosition = MomKnifeAttack.advance(Vec3.ZERO, launch, 0.3);
        assertFalse(MomKnifeAttack.contacts(Vec3.ZERO, finalPosition, launch,
                new AABB(100, 100, 100, 101, 101, 101)));
        assertEquals(0.3, finalPosition.length(), 1e-8);
    }

    @Test
    void retractionHasTheOppositeThrustVelocityAndKeepsTheOriginalAimPosition() {
        Vec3 origin = new Vec3(4, 5, -7);
        MomKnifePose pose = new MomKnifePose(37, 123, 0);
        Vec3 forward = pose.bladePoint(Vec3.ZERO, 1);
        Vec3 fired = origin.add(forward.scale(5));
        Vec3 retracted = MomKnifeAttack.retract(fired, origin);
        assertVector(forward.scale(-MomKnifeAttack.THRUST_SPEED), retracted.subtract(fired));
        for (int tick = 0; tick < 4; tick++) retracted = MomKnifeAttack.retract(retracted, origin);
        assertEquals(origin, retracted);
    }

    @Test
    void retractionClampsTheLastStepAndHoldsAtTheAimAnchor() {
        Vec3 origin = new Vec3(7, 8, 9);
        assertEquals(origin, MomKnifeAttack.retract(origin.add(0.1, -0.2, 0.3), origin));
        assertEquals(origin, MomKnifeAttack.retract(origin, origin));
    }

    @Test
    void rearTargetOnlyLiftsTheBladeWithoutTurningOrRollingAtAnyFormationHeading() {
        for (float yaw : new float[]{0, 40, -40, 80, -80, 179, -179}) {
            MomKnifePose start = new MomKnifePose(yaw, 0, 0);
            double radians = Math.toRadians(yaw);
            Vec3 rearTarget = new Vec3(Math.sin(radians) * 4, 0, -Math.cos(radians) * 4);
            for (double age : new double[]{0, 0.5, 1, 1.5, 2, 2.5, 3}) {
                MomKnifePose aiming = MomKnifeAttack.aim(Vec3.ZERO, rearTarget, start, age, 3);
                // The game's fast atan2 is approximate; millidegree drift is not a turn.
                assertEquals(yaw, aiming.yaw(), 1e-3);
                assertEquals(0, aiming.twist(), 1e-6);
                assertTrue(aiming.pitch() <= 0 && aiming.pitch() >= -90);
            }
            MomKnifePose launched = MomKnifeAttack.aim(Vec3.ZERO, rearTarget, start, 3, 3);
            assertTrue(rearTarget.normalize().distanceTo(launched.bladePoint(Vec3.ZERO, 1)) < 2e-5);
        }
    }

    @Test
    void returnEasesBothAnglesFromLaunchPoseAndFinishesFacingTheLatestOwnerHeading() {
        MomKnifePose start = new MomKnifePose(179, 120, 0);
        assertEquals(start, MomKnifeAttack.returnPose(-179, start, 0));
        MomKnifePose half = MomKnifeAttack.returnPose(-179, start, 4);
        assertEquals(180, half.yaw(), 1e-6);
        assertEquals(60, half.pitch(), 1e-6);
        MomKnifePose end = MomKnifeAttack.returnPose(90, start, 8);
        assertEquals(90, end.yaw(), 1e-6);
        assertEquals(0, end.pitch());
        assertEquals(0, end.twist());
    }

    private record Step(Vec3 position, MomKnifePose pose, boolean hit) {}

    private static Step thrust(Vec3 origin, AABB target) {
        MomKnifePose pose = new MomKnifePose(0, 90, 0);
        Vec3 next = MomKnifeAttack.advance(origin, pose, 8);
        return new Step(next, pose, MomKnifeAttack.contacts(origin, next, pose, target));
    }

    private static void assertVector(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, 1e-6);
        assertEquals(expected.y, actual.y, 1e-6);
        assertEquals(expected.z, actual.z, 1e-6);
    }
}
