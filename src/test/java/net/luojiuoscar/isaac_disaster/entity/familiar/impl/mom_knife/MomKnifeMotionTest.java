package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MomKnifeMotionTest {
    private static final double EPSILON = 1.0e-6;
    private static final Vec3 OWNER = new Vec3(10, 20, 30);

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 6})
    void clientPitchFinishesAimingAtElevatedTargetsBeforeLaunch(int duration) {
        for (Vec3 target : new Vec3[]{new Vec3(0, Math.sqrt(3), 1), new Vec3(0, 1, 0)}) {
            MomKnifeMotion motion = new MomKnifeMotion();
            MomKnifePose start = new MomKnifePose(0, 0, 0);
            for (int age = 1; age <= duration; age++) {
                MomKnifePose authoritative = MomKnifeAttack.aim(Vec3.ZERO, target, start, age, duration);
                motion.turnPitchTo(authoritative.pitch(), MomKnifeMotion.ATTACK_PITCH_STEP);
            }
            float expectedPitch = target.z == 0 ? -180 : -150;
            assertEquals(expectedPitch, motion.renderPitch(1), EPSILON,
                    "Client smoothing must finish the shortest aim before the first thrust step");
        }
    }

    @Test
    void formationLayersAdvanceOnlyWhenTheCurrentArcIsFull() {
        assertEquals(1, MomKnifeMotion.formationLayers(0));
        assertEquals(1, MomKnifeMotion.formationLayers(1));
        assertEquals(1, MomKnifeMotion.formationLayers(5));
        assertEquals(2, MomKnifeMotion.formationLayers(6));
        assertEquals(2, MomKnifeMotion.formationLayers(14));
        assertEquals(3, MomKnifeMotion.formationLayers(15));
        assertEquals(3, MomKnifeMotion.formationLayers(27));
        assertEquals(4, MomKnifeMotion.formationLayers(28));
    }

    @Test
    void followClosesThirtyFivePercentOfTheRemainingDistance() {
        assertPosition(new Vec3(1.35, 2.7, 2.65),
                MomKnifeMotion.follow(new Vec3(1, 2, 3), new Vec3(2, 4, 2)));
    }

    @Test
    void followCapsMovementLengthWhileKeepingDirection() {
        assertPosition(new Vec3(0.9, 1.2, 0), MomKnifeMotion.follow(Vec3.ZERO, new Vec3(30, 40, 0)));
        assertPosition(new Vec3(0.35, 0, 0), MomKnifeMotion.follow(Vec3.ZERO, new Vec3(1, 0, 0)));
        assertPosition(Vec3.ZERO, MomKnifeMotion.follow(Vec3.ZERO, Vec3.ZERO));
    }

    @Test
    void flyCapsSpeedAndStopsAtTheTargetWithoutOvershooting() {
        assertPosition(new Vec3(0.3, 0.4, 0), MomKnifeMotion.fly(Vec3.ZERO, new Vec3(3, 4, 0), 0.5));
        assertPosition(new Vec3(3, 4, 0), MomKnifeMotion.fly(Vec3.ZERO, new Vec3(3, 4, 0), 6));
        assertSame(Vec3.ZERO, MomKnifeMotion.fly(Vec3.ZERO, Vec3.ZERO, 1));
    }

    @Test
    void ticksWithoutNewPacketsFinishTheCountdownAndThenHold() {
        MomKnifeMotion motion = new MomKnifeMotion();
        motion.acceptPosition(new Vec3(9, 6, -3), 3);
        Vec3 current = motion.tickPosition(Vec3.ZERO);
        assertPosition(new Vec3(3, 2, -1), current);
        current = motion.tickPosition(current);
        assertPosition(new Vec3(6, 4, -2), current);
        current = motion.tickPosition(current);
        assertPosition(new Vec3(9, 6, -3), current);
        assertSame(current, motion.tickPosition(current));
        Vec3 externalPosition = new Vec3(50, 20, 10);
        assertSame(externalPosition, motion.tickPosition(externalPosition));
    }

    @Test
    void redundantPositionTargetsDoNotPostponeArrival() {
        MomKnifeMotion motion = new MomKnifeMotion();
        motion.acceptPosition(new Vec3(9, 0, 0), 3);
        Vec3 current = motion.tickPosition(Vec3.ZERO);
        motion.acceptPosition(new Vec3(9, 0, 0), 3);
        current = motion.tickPosition(current);
        assertPosition(new Vec3(6, 0, 0), current);
        motion.acceptPosition(new Vec3(9, 0, 0), 20);
        assertPosition(new Vec3(9, 0, 0), motion.tickPosition(current));
    }

    @Test
    void retargetingStartsAtTheLatestSuppliedPosition() {
        MomKnifeMotion motion = new MomKnifeMotion();
        motion.acceptPosition(new Vec3(12, 0, 0), 3);
        assertPosition(new Vec3(4, 0, 0), motion.tickPosition(Vec3.ZERO));
        motion.acceptPosition(new Vec3(15, 6, -3), 2);
        assertPosition(new Vec3(12, 4, -2), motion.tickPosition(new Vec3(9, 2, -1)));
    }

    @Test
    void latestBatchedTargetWinsAndNonpositiveStepCountsTakeOneTick() {
        MomKnifeMotion motion = new MomKnifeMotion();
        motion.acceptPosition(new Vec3(100, 100, 100), 20);
        motion.acceptPosition(new Vec3(8, -4, 2), 2);
        assertPosition(new Vec3(4, -2, 1), motion.tickPosition(Vec3.ZERO));
        motion.acceptPosition(new Vec3(3, 2, 1), 0);
        assertPosition(new Vec3(3, 2, 1), motion.tickPosition(Vec3.ZERO));
        motion.acceptPosition(new Vec3(-3, -2, -1), -5);
        assertPosition(new Vec3(-3, -2, -1), motion.tickPosition(Vec3.ZERO));
    }

    @Test
    void firstHeadingInitializesImmediatelyAndLaterTurnsRespectTheLimit() {
        MomKnifeMotion motion = new MomKnifeMotion();
        motion.turnTo(120, 10);
        assertEquals(120, motion.renderYaw(0), EPSILON);
        assertEquals(120, motion.renderYaw(1), EPSILON);
        motion.turnTo(170, 10);
        assertEquals(120, motion.renderYaw(0), EPSILON);
        assertEquals(125, motion.renderYaw(0.5f), EPSILON);
        assertEquals(130, motion.renderYaw(1), EPSILON);
        motion.turnTo(134, 10);
        assertEquals(134, motion.renderYaw(1), EPSILON);
    }

    @Test
    void headingCrossesBothAngleBoundariesAlongTheShortestArc() {
        MomKnifeMotion positive = new MomKnifeMotion();
        positive.turnTo(179, 10);
        positive.turnTo(-179, 10);
        assertEquals(179, positive.renderYaw(0), EPSILON);
        assertEquals(180, positive.renderYaw(0.5f), EPSILON);
        assertEquals(181, positive.renderYaw(1), EPSILON);
        MomKnifeMotion negative = new MomKnifeMotion();
        negative.turnTo(-179, 10);
        negative.turnTo(179, 10);
        assertEquals(-180, negative.renderYaw(0.5f), EPSILON);
        assertEquals(-181, negative.renderYaw(1), EPSILON);
    }

    @Test
    void renderingSamplesDoNotAdvanceTheHeadingBetweenTicks() {
        MomKnifeMotion motion = new MomKnifeMotion();
        motion.turnTo(-30, 20);
        motion.turnTo(80, 20);
        assertEquals(-25, motion.renderYaw(0.25f), EPSILON);
        assertEquals(-15, motion.renderYaw(0.75f), EPSILON);
        assertEquals(-25, motion.renderYaw(0.25f), EPSILON);
        assertEquals(-10, motion.renderYaw(1), EPSILON);
    }

    @Test
    void resetCancelsPositionAndLetsTheNextHeadingInitializeDirectly() {
        MomKnifeMotion motion = new MomKnifeMotion();
        motion.acceptPosition(new Vec3(20, 0, 0), 5);
        motion.turnTo(120, 10);
        motion.turnTo(170, 10);
        motion.reset();
        assertSame(Vec3.ZERO, motion.tickPosition(Vec3.ZERO));
        motion.turnTo(-90, 1);
        assertEquals(-90, motion.renderYaw(0), EPSILON);
        assertEquals(-90, motion.renderYaw(1), EPSILON);
        motion.acceptPosition(new Vec3(20, 0, 0), 2);
        assertPosition(new Vec3(10, 0, 0), motion.tickPosition(Vec3.ZERO));
    }

    @Test
    void pitchIsInterpolatedBetweenTicksAndReturnsSmoothlyToNeutral() {
        MomKnifeMotion motion = new MomKnifeMotion();
        motion.turnPitchTo(120, 60);
        assertEquals(0, motion.renderPitch(0), EPSILON);
        assertEquals(30, motion.renderPitch(0.5f), EPSILON);
        assertEquals(60, motion.renderPitch(1), EPSILON);
        motion.turnPitchTo(90, 60);
        assertEquals(75, motion.renderPitch(0.5f), EPSILON);
        motion.turnPitchTo(0, 22.5f);
        assertEquals(67.5, motion.renderPitch(1), EPSILON);
        motion.reset();
        assertEquals(0, motion.renderPitch(0), EPSILON);
        assertEquals(0, motion.renderPitch(1), EPSILON);
    }

    @Test
    void centerSitsBehindOwnerAtEveryCardinalYaw() {
        assertPosition(new Vec3(10, 21.4, 29.05), position(0, 0));
        assertPosition(new Vec3(10.95, 21.4, 30), position(90, 0));
        assertPosition(new Vec3(10, 21.4, 30.95), position(180, 0));
        assertPosition(new Vec3(9.05, 21.4, 30), position(270, 0));
    }

    @Test
    void pairsRemainSymmetricAroundRearDirectionAtEveryCardinalYaw() {
        for (float yaw : new float[]{0, 90, 180, 270}) {
            Vec3 rear = position(yaw, 0).subtract(OWNER.add(0, 1.4, 0)).normalize();
            for (int leftIndex : new int[]{1, 3}) {
                Vec3 left = position(yaw, leftIndex).subtract(OWNER.add(0, 1.4, 0));
                Vec3 right = position(yaw, leftIndex + 1).subtract(OWNER.add(0, 1.4, 0));
                assertEquals(left.dot(rear), right.dot(rear), EPSILON);
                assertEquals(0, left.add(right).cross(rear).length(), EPSILON);
                assertEquals(0.95, left.length(), EPSILON);
                assertEquals(0.95, right.length(), EPSILON);
                assertTrue(left.dot(rear) > 0);
                assertTrue(right.dot(rear) > 0);
            }
        }
    }

    @Test
    void firstRingAlternatesFromCenterThroughFortyAndEightyDegrees() {
        assertPosition(new Vec3(-0.6106482292022123, 1.4, -0.7277422209635291),
                MomKnifeMotion.formationPosition(Vec3.ZERO, 0.6, 2, 0, 1));
        assertPosition(new Vec3(0.6106482292022123, 1.4, -0.7277422209635291),
                MomKnifeMotion.formationPosition(Vec3.ZERO, 0.6, 2, 0, 2));
        assertPosition(new Vec3(-0.9355673653615977, 1.4, -0.16496576878358388),
                MomKnifeMotion.formationPosition(Vec3.ZERO, 0.6, 2, 0, 3));
        assertPosition(new Vec3(0.9355673653615977, 1.4, -0.16496576878358388),
                MomKnifeMotion.formationPosition(Vec3.ZERO, 0.6, 2, 0, 4));
    }

    @Test
    void overflowStartsWiderRingsWithIncreasingCapacity() {
        assertPosition(new Vec3(10, 21.4, 28.5), position(0, 5));
        assertPosition(new Vec3(9.486969785011497, 21.4, 28.590461068821137), position(0, 6));
        assertPosition(new Vec3(11.477211629518312, 21.4, 29.739527733499606), position(0, 13));
        assertPosition(new Vec3(10, 21.4, 27.95), position(0, 14));
        assertPosition(new Vec3(10, 21.4, 27.4), position(0, 27));
    }

    @Test
    void ownerWidthExpandsFormationAndNegativeIndexUsesCenter() {
        assertPosition(new Vec3(10, 21.4, 28.35), MomKnifeMotion.formationPosition(OWNER, 2, 2, 0, 0));
        assertPosition(new Vec3(10, 21.4, 29.35), MomKnifeMotion.formationPosition(OWNER, -2, 2, 0, 0));
        assertPosition(position(90, 0), position(90, -100));
    }


    @Test
    void facingYawTracksHorizontalDirectionAndPreservesHeadingForVerticalTargets() {
        Vec3 anchor = new Vec3(2, 3, 4);
        assertEquals(0, MomKnifeMotion.facingYaw(anchor, anchor.add(0, 100, 2), 37), 1e-4);
        assertEquals(90, MomKnifeMotion.facingYaw(anchor, anchor.add(-2, 0, 0), 37), 1e-4);
        assertEquals(-90, MomKnifeMotion.facingYaw(anchor, anchor.add(2, 0, 0), 37), 1e-4);
        assertEquals(37, MomKnifeMotion.facingYaw(anchor, anchor.add(0, 100, 0), 37), 1e-4);
        assertEquals(37, MomKnifeMotion.facingYaw(anchor, anchor, 37), 1e-4);
    }

    private static Vec3 position(float yaw, int index) {
        return MomKnifeMotion.formationPosition(OWNER, 0.6, 2, yaw, index);
    }

    private static void assertPosition(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.z, actual.z, EPSILON);
    }
}
