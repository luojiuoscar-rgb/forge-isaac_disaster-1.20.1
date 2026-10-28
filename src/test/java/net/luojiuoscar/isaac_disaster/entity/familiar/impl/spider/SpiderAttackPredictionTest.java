package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpiderAttackPredictionTest {
    @Test void stationarySlowAndTouchingTargetsHaveNoRandomAimOffset() {
        var prediction = new SpiderAttackPrediction(1.15, 1);
        Vec3 target = new Vec3(4, 2, 0);
        assertEquals(target, prediction.goal(Vec3.ZERO, target, Vec3.ZERO, 6, 0.36));
        assertEquals(target, prediction.goal(Vec3.ZERO, target, new Vec3(0.01, 1, 0), 6, 0.36));
        assertEquals(target, prediction.goal(new Vec3(3.9, 2, 0), target, new Vec3(0, 0, 0.2), 6, 0.36));
    }

    @Test void lateralTargetIsMetAtItsFuturePositionAtTheActualAttackSpeed() {
        var prediction = new SpiderAttackPrediction(1, 0);
        Vec3 target = new Vec3(4, 0, 0);
        Vec3 goal = prediction.goal(Vec3.ZERO, target, new Vec3(0, 0, 0.2), 0, 0.36);
        double time = goal.z / 0.2;
        assertTrue(goal.z > 1);
        assertEquals(goal.horizontalDistance() / SpiderMovement.MAX_SPEED, time, 1e-8);
        assertEquals(4, goal.x, 1e-8);
    }

    @Test void turningTimeIsIncludedButPredictionDistanceAndHorizonStayBounded() {
        var prediction = new SpiderAttackPrediction(1, 0);
        Vec3 target = new Vec3(4, 0, 0), velocity = new Vec3(0, 0, 0.1);
        assertTrue(prediction.goal(Vec3.ZERO, target, velocity, 6, 0.36).z
                > prediction.goal(Vec3.ZERO, target, velocity, 0, 0.36).z);
        Vec3 far = new Vec3(100, 2, 100);
        Vec3 goal = new SpiderAttackPrediction(1.15, 1).goal(Vec3.ZERO, far, new Vec3(1, 20, 1), 100, 2);
        assertTrue(goal.subtract(far).horizontalDistance() <= SpiderAttackPrediction.MAX_LEAD_DISTANCE + 0.25);
        assertEquals(far.y, goal.y);
        Vec3 slowGoal = prediction.goal(Vec3.ZERO, far, new Vec3(0.1, 0, 0), 100, 0.36);
        assertEquals(0.1 * SpiderAttackPrediction.MAX_PREDICTION_TICKS, slowGoal.x - far.x, 1e-8);
    }

    @Test void eachAttemptSpreadsItsAimWithoutChangingItOnEveryLookup() {
        Vec3 target = new Vec3(4, 0, 0), velocity = new Vec3(0, 0, 0.2);
        var left = new SpiderAttackPrediction(0.85, -1);
        var right = new SpiderAttackPrediction(1.15, 1);
        Vec3 a = left.goal(Vec3.ZERO, target, velocity, 0, 0.36);
        Vec3 b = right.goal(Vec3.ZERO, target, velocity, 0, 0.36);
        assertNotEquals(a, b);
        assertTrue(a.z > 0 && b.z > a.z);
        assertEquals(a, left.goal(Vec3.ZERO, target, velocity, 0, 0.36));
    }

    @Test void smallMotionKeepsTheSegmentButTurnsLargeDeviationsAndPassingTheGoalEndIt() {
        var prediction = new SpiderAttackPrediction(1, 0);
        Vec3 target = new Vec3(4, 0, 0), velocity = new Vec3(0, 0, 0.2);
        prediction.depart(Vec3.ZERO, new Vec3(4, 0, 1.7), target, velocity, 100);
        assertFalse(prediction.obsolete(new Vec3(0.4, 0, 0.17), new Vec3(4, 0, 0.2), velocity, 101, 0.36));
        assertTrue(prediction.obsolete(Vec3.ZERO, new Vec3(4, 0, 4), velocity, 101, 0.36));
        assertTrue(prediction.obsolete(Vec3.ZERO, new Vec3(4, 0, 0.2), velocity.scale(-1), 101, 0.36));
        assertFalse(prediction.obsolete(new Vec3(3.8, 0, 0), new Vec3(4, 0, 0), velocity.scale(-1), 101, 0.36),
                "An imminent real contact is not abandoned just because the enemy turns");
        prediction.depart(Vec3.ZERO, new Vec3(1, 0, 0), target, Vec3.ZERO, 100);
        assertTrue(prediction.passedGoal(new Vec3(1.1, 0, 0)));
        assertTrue(prediction.obsolete(new Vec3(1.1, 0, 0), target, Vec3.ZERO, 104, 0.36));
    }

    @Test void sharedMotionUsesObservedPositionsAndResetsOnStopsTeleportsAndStaleSamples() {
        var motion = new SpiderAttackPrediction.Motion();
        motion.sample(Vec3.ZERO, 100);
        assertEquals(Vec3.ZERO, motion.velocity());
        motion.sample(new Vec3(0.2, 0.1, 0), 101);
        assertEquals(new Vec3(0.2, 0, 0), motion.velocity());
        motion.sample(new Vec3(100, 0, 0), 101);
        assertEquals(new Vec3(0.2, 0, 0), motion.velocity(), "Repeated consumers do not resample the same tick");
        motion.sample(new Vec3(0, 0.1, 0), 102);
        assertEquals(new Vec3(-0.2, 0, 0), motion.velocity(), "A reversal does not inherit stale forward velocity");
        motion.sample(new Vec3(0, 0.1, 0), 103);
        assertEquals(Vec3.ZERO, motion.velocity());
        motion.sample(new Vec3(20, 0.1, 0), 104);
        assertEquals(Vec3.ZERO, motion.velocity());
        motion.sample(new Vec3(21, 0.1, 0), 110);
        assertEquals(Vec3.ZERO, motion.velocity());
        motion.sample(Vec3.ZERO, 99);
        assertEquals(Vec3.ZERO, motion.velocity());
    }
}
