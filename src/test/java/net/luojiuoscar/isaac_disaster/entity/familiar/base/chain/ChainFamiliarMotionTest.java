package net.luojiuoscar.isaac_disaster.entity.familiar.base.chain;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainFamiliarMotionTest {
    private static final double EPSILON = 1.0e-6;

    @Test
    void smallAbsoluteCorrectionsInterpolateAndOnlyLargeRecallsReset() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 target = new Vec3(0.6, 0, 0);
        assertTrue(!motion.acceptPosition(Vec3.ZERO, target, 3, true));
        Vec3 current = motion.tickPosition(Vec3.ZERO);
        assertPosition(new Vec3(0.2, 0, 0), current);
        assertTrue(!motion.acceptPosition(current, target, 3, true));
        current = motion.tickPosition(current);
        assertPosition(new Vec3(0.4, 0, 0), current);
        assertPosition(target, motion.tickPosition(current));
        Vec3 recalled = new Vec3(30, 20, 10);
        assertTrue(motion.acceptPosition(target, recalled, 3, true));
        assertSame(recalled, motion.tickPosition(recalled));
    }

    @Test
    void periodicAbsolutePacketsDoNotIntroduceJumpsDuringLongPursuit() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 current = Vec3.ZERO;
        double previousSpeed = 0;
        for (int tick = 1; tick <= 900; tick++) {
            Vec3 target = new Vec3(tick * 0.16, 0, 0);
            assertTrue(!motion.acceptPosition(current, target, 3, tick % 401 == 0));
            Vec3 next = motion.tickPosition(current);
            double speed = next.distanceTo(current);
            assertTrue(speed > 0 && speed <= 0.16001);
            assertTrue(Math.abs(speed - previousSpeed) < 0.06);
            previousSpeed = speed;
            current = next;
        }
    }

    @Test
    void continuouslyFollowsAtShortDistancesWithoutTheOldFourBlockGate() {
        for (double distance : new double[]{1.1, 1.5, 2, 3, 4}) {
            ChainFamiliarMotion motion = new ChainFamiliarMotion();
            Vec3 next = motion.follow(Vec3.ZERO, new Vec3(distance, 0, 0));
            assertTrue(next.x > 0 && next.x < 0.3, "Every distance beyond 1 starts gently: " + distance);
        }
    }

    @Test
    void remainsAtRestAnywhereInsideOneBlockIncludingOverlap() {
        for (Vec3 start : new Vec3[]{Vec3.ZERO, new Vec3(0.5, 0, 0), new Vec3(0, 0.9, 0), new Vec3(1, 0, 0)}) {
            ChainFamiliarMotion motion = new ChainFamiliarMotion();
            for (int i = 0; i < 100; i++) {
                assertSame(start, motion.follow(start, Vec3.ZERO), "An idle follower must not be pushed back to a one-block shell");
            }
        }
    }

    @Test
    void enteringInnerAreaDampsMomentumWithoutPushingBackOutward() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 current = new Vec3(5, 0, 0);
        for (int i = 0; i < 4; i++) current = motion.follow(current, Vec3.ZERO);
        Vec3 target = current.add(-0.8, 0, 0);
        Vec3 next = motion.follow(current, target);
        assertTrue(next.x < current.x && next.distanceTo(current) < 0.5, "Keep braking instead of hard stopping inside the area");
        for (int i = 0; i < 100; i++) {
            current = next;
            next = motion.follow(current, target);
            assertTrue(next.x <= current.x, "No outward force should restore the old exact spacing");
        }
        assertTrue(next.distanceTo(target) < 1, "Braking may finish anywhere inside the follow area");
        assertSame(next, motion.follow(next, target));
    }

    @Test
    void acceleratesThenBrakesToAStableParkingPositionWithoutRebound() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 current = new Vec3(5, 0, 0);
        double previousSpeed = 0;
        double peakSpeed = 0;
        for (int i = 0; i < 100; i++) {
            Vec3 next = motion.follow(current, Vec3.ZERO);
            double speed = next.distanceTo(current);
            assertTrue(next.x <= current.x && next.x >= 0.99, "Parking must approach monotonically without crossing the comfort area");
            assertTrue(Math.abs(speed - previousSpeed) < 0.3, "Startup and braking must change speed gradually");
            if (speed == 0 && previousSpeed > 0) assertTrue(previousSpeed < 0.01, "Only negligible drift may be snapped to rest");
            peakSpeed = Math.max(peakSpeed, speed);
            previousSpeed = speed;
            current = next;
        }
        assertTrue(peakSpeed > 0.25, "The follower still catches up decisively");
        assertEquals(1.0, current.x, 0.01);
        assertEquals(0, previousSpeed, EPSILON, "Parking must finish rather than drift forever");
        assertPosition(current, motion.follow(current, Vec3.ZERO));
        assertTrue(motion.follow(new Vec3(3, 0, 0), Vec3.ZERO).x < 3);
        assertTrue(motion.follow(new Vec3(5, 0, 0), Vec3.ZERO).x < 5);
    }

    @Test
    void enteringTwoBlocksDoesNotInstantlyEraseVelocity() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 current = new Vec3(5, 0, 0);
        for (int i = 0; i < 4; i++) current = motion.follow(current, Vec3.ZERO);
        Vec3 nearbyOwner = current.add(-1.9, 0, 0);
        Vec3 next = motion.follow(current, nearbyOwner);
        assertTrue(next.x < current.x, "Keep braking gently after the owner enters the inner radius");
        assertTrue(next.distanceTo(current) < 0.5, "Braking must not launch the familiar through the owner");
        for (int i = 0; i < 80; i++) next = motion.follow(next, nearbyOwner);
        assertSame(next, motion.follow(next, nearbyOwner));
    }

    @Test
    void pureHeightDifferenceStartsSmoothFollowing() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 next = motion.follow(Vec3.ZERO, new Vec3(0, 5, 0));
        assertTrue(next.y > 0 && next.y < 0.3);
        assertEquals(0, next.x, EPSILON);
        assertEquals(0, next.z, EPSILON);
    }

    @Test
    void continuouslyMovingOwnerDoesNotCauseRepeatedStartsAndStops() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 current = new Vec3(-5, 0, 0);
        Vec3 previousVelocity = Vec3.ZERO;
        for (int i = 0; i < 240; i++) {
            Vec3 target = new Vec3(i * 0.22, 0, 0);
            Vec3 next = motion.follow(current, target);
            Vec3 velocity = next.subtract(current);
            assertTrue(velocity.length() <= 1.5 + EPSILON);
            assertTrue(velocity.subtract(previousVelocity).length() < 0.3);
            if (i > 40) assertTrue(velocity.x > 0.15 && velocity.x < 0.3, "Walking pursuit should settle into a continuous pace");
            current = next;
            previousVelocity = velocity;
        }
    }

    @Test
    void suddenTurnChangesVelocityGraduallyAndStillSettles() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 current = new Vec3(6, 0, 0);
        Vec3 before = current;
        for (int i = 0; i < 4; i++) {
            before = current;
            current = motion.follow(current, Vec3.ZERO);
        }
        Vec3 previousVelocity = current.subtract(before);
        Vec3 target = current.add(0, 0, 5);
        Vec3 next = motion.follow(current, target);
        assertTrue(next.subtract(current).subtract(previousVelocity).length() < 0.5,
                "Turning must preserve and damp momentum rather than redirect it instantly");
        for (int i = 0; i < 100; i++) next = motion.follow(next, target);
        assertTrue(next.distanceTo(target) <= 2);
        assertSame(next, motion.follow(next, target));
    }

    @Test
    void distantPursuitRespectsTheKnifeSpeedLimit() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 current = Vec3.ZERO;
        for (int i = 0; i < 120; i++) {
            Vec3 next = motion.follow(current, new Vec3(i * 2 + 30, 40, 0));
            assertTrue(next.distanceTo(current) <= 1.5 + EPSILON);
            current = next;
        }
    }

    @Test
    void resetCancelsThePreviousPursuitAndItsMomentum() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        Vec3 current = Vec3.ZERO;
        for (int i = 0; i < 4; i++) current = motion.follow(current, new Vec3(8, 0, 0));
        motion.reset();
        assertPosition(new ChainFamiliarMotion().follow(current, current.add(3, 0, 0)),
                motion.follow(current, current.add(3, 0, 0)));
    }

    @Test
    void clientFinishesInterpolationAndHoldsWithoutMorePackets() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        motion.acceptPosition(Vec3.ZERO, new Vec3(9, 6, -3), 3, false);
        Vec3 current = motion.tickPosition(Vec3.ZERO);
        assertPosition(new Vec3(3, 2, -1), current);
        current = motion.tickPosition(current);
        assertPosition(new Vec3(6, 4, -2), current);
        current = motion.tickPosition(current);
        assertPosition(new Vec3(9, 6, -3), current);
        assertSame(current, motion.tickPosition(current));
    }

    @Test
    void rotationOnlyPositionUpdatesDoNotDelayArrival() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        motion.acceptPosition(Vec3.ZERO, new Vec3(9, 0, 0), 3, false);
        Vec3 current = motion.tickPosition(Vec3.ZERO);
        motion.acceptPosition(Vec3.ZERO, new Vec3(9, 0, 0), 3, false);
        current = motion.tickPosition(current);
        assertPosition(new Vec3(6, 0, 0), current);
        motion.acceptPosition(Vec3.ZERO, new Vec3(9, 0, 0), 3, false);
        assertPosition(new Vec3(9, 0, 0), motion.tickPosition(current));
    }

    @Test
    void clientRetargetsFromCurrentPositionAndClampsNonpositiveSteps() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        motion.acceptPosition(Vec3.ZERO, new Vec3(9, 0, 0), 3, false);
        Vec3 current = motion.tickPosition(Vec3.ZERO);
        motion.acceptPosition(Vec3.ZERO, new Vec3(7, 4, 0), 2, false);
        assertPosition(new Vec3(5, 2, 0), motion.tickPosition(current));
        motion.acceptPosition(Vec3.ZERO, new Vec3(1, 2, 3), -1, false);
        assertPosition(new Vec3(1, 2, 3), motion.tickPosition(current));
    }

    @Test
    void resetPreventsInterpolationBackToTheOldLocation() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        motion.acceptPosition(Vec3.ZERO, new Vec3(9, 0, 0), 3, false);
        motion.tickPosition(Vec3.ZERO);
        motion.reset();
        Vec3 recalled = new Vec3(50, 20, 10);
        assertSame(recalled, motion.tickPosition(recalled));
    }

    @Test
    void yawUsesTheShortArcAndResetsAfterTeleport() {
        ChainFamiliarMotion motion = new ChainFamiliarMotion();
        motion.turnTo(179);
        motion.turnTo(-179);
        assertEquals(180, motion.renderYaw(0.5F), EPSILON);
        assertEquals(181, motion.renderYaw(1), EPSILON);
        motion.turnTo(-90);
        assertEquals(199, motion.renderYaw(1), EPSILON);
        motion.reset();
        motion.turnTo(-90);
        assertEquals(-90, motion.renderYaw(0), EPSILON);
        assertEquals(-90, motion.renderYaw(1), EPSILON);
    }

    private static void assertPosition(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
