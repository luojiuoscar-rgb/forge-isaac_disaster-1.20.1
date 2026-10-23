package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlyClientInterpolationTest {
    @Test
    void periodicAbsolutePacketsKeepTheSameMotionAsRelativeUpdates() {
        FlyClientInterpolation relative = new FlyClientInterpolation();
        FlyClientInterpolation corrected = new FlyClientInterpolation();
        var expected = new FlyClientInterpolation.Pose(new Vec3(2.1, 0, 0), 0, 0);
        var actual = expected;
        double phase = 0;
        for (int tick = 1; tick <= 1000; tick++) {
            phase = FlyOrbitLayout.advance(phase, 2.1);
            Vec3 target = FlyOrbitLayout.position(Vec3.ZERO, 2.1, 0, 0, 8, phase);
            float yaw = (float) Math.toDegrees(phase);
            Vec3 previous = actual.position();
            expected = receive(relative, expected, target, yaw, 3, false);
            actual = receive(corrected, actual, target, yaw, 3, tick % 401 == 0);
            assertEquals(0, expected.position().distanceTo(actual.position()), 1e-9,
                    "Absolute correction must preserve smooth motion at tick " + tick);
            assertTrue(previous.distanceTo(actual.position()) <= 0.300001,
                    "A stationary owner's ring must not lurch forward at tick " + tick);
        }
    }

    @Test
    void largeAbsoluteRecallDiscardsTheOldInterpolationTarget() {
        FlyClientInterpolation interpolation = new FlyClientInterpolation();
        assertTrue(interpolation.update(Vec3.ZERO, new Vec3(1, 0, 0), 0, 0, 3, false));
        Vec3 recalled = new Vec3(20, 0, 0);
        assertFalse(interpolation.update(Vec3.ZERO, recalled, 90, 0, 3, true));
        assertNull(interpolation.tick(recalled, 90, 0), "Old targets must not pull a recalled fly back");
    }

    @Test
    void nearbyAbsoluteCorrectionInterpolatesRotationAcrossTheAngleBoundary() {
        FlyClientInterpolation interpolation = new FlyClientInterpolation();
        assertTrue(interpolation.update(Vec3.ZERO, new Vec3(0.6, 0, 0), -179, 12, 3, true));
        var pose = new FlyClientInterpolation.Pose(Vec3.ZERO, 179, 0);
        for (int tick = 0; tick < 3; tick++) {
            var next = interpolation.tick(pose.position(), pose.yaw(), pose.pitch());
            assertNotNull(next);
            assertTrue(Math.abs(next.yaw() - pose.yaw()) < 1, "Use the short rotation across 180 degrees");
            pose = next;
        }
        assertEquals(0.6, pose.position().x, 1e-9);
        assertEquals(0, Mth.wrapDegrees(pose.yaw() + 179), 1e-5);
        assertEquals(12, pose.pitch(), 1e-5);
        assertNull(interpolation.tick(pose.position(), pose.yaw(), pose.pitch()));
    }

    @Test
    void nonPositiveInterpolationStepsFinishInOneTick() {
        for (int steps : new int[]{0, -1}) {
            FlyClientInterpolation interpolation = new FlyClientInterpolation();
            Vec3 target = new Vec3(1, 2, 3);
            assertTrue(interpolation.update(Vec3.ZERO, target, 10, 20, steps, false));
            var pose = interpolation.tick(Vec3.ZERO, 0, 0);
            assertEquals(target, pose.position());
            assertNull(interpolation.tick(pose.position(), pose.yaw(), pose.pitch()));
        }
    }

    @Test
    void onlyLargeAbsoluteCorrectionsSnapWhileRelativeUpdatesKeepTheirExistingBehavior() {
        FlyClientInterpolation interpolation = new FlyClientInterpolation();
        assertTrue(interpolation.update(Vec3.ZERO, new Vec3(4, 0, 0), 0, 0, 3, true));
        assertFalse(interpolation.update(Vec3.ZERO, new Vec3(4.01, 0, 0), 0, 0, 3, true));
        assertTrue(interpolation.update(Vec3.ZERO, new Vec3(20, 0, 0), 0, 0, 3, false));
    }

    private FlyClientInterpolation.Pose receive(FlyClientInterpolation interpolation,
                                                FlyClientInterpolation.Pose current, Vec3 target,
                                                float yaw, int steps, boolean absolute) {
        if (!interpolation.update(current.position(), target, yaw, 0, steps, absolute)) {
            return new FlyClientInterpolation.Pose(target, yaw, 0);
        }
        return interpolation.tick(current.position(), current.yaw(), current.pitch());
    }
}
