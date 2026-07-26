package net.luojiuoscar.isaac_disaster.system.flight;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsaacFlightRulesTest {
    private static final double EPSILON = 1.0E-9;

    @Test
    void speedCapUsesTheConfiguredMultiplierUntilTheAbsoluteLimit() {
        assertEquals(0.2D, IsaacFlightRules.calculateSpeedCap(0.1D, 2.0D, 0.5D), EPSILON);
        assertEquals(0.15D, IsaacFlightRules.calculateSpeedCap(0.1D, 3.0D, 0.15D), EPSILON);
    }

    @Test
    void nextVelocityApproachesTheFullLookDirection() {
        Vec3 next = IsaacFlightRules.calculateNextVelocity(
                new Vec3(0.2D, 0.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                0.2D,
                0.25D
        );

        assertEquals(0.15D, next.x, EPSILON);
        assertEquals(0.0D, next.y, EPSILON);
        assertEquals(0.05D, next.z, EPSILON);
    }

    @Test
    void horizontalLookCancelsDownwardVelocityWhenFullySteered() {
        Vec3 next = IsaacFlightRules.calculateNextVelocity(
                new Vec3(0.0D, -0.08D, 0.0D),
                new Vec3(1.0D, 0.0D, 0.0D),
                0.2D,
                1.0D
        );

        assertEquals(0.0D, next.y, EPSILON);
    }

    @Test
    void lookingUpTargetsUpwardFlight() {
        Vec3 next = IsaacFlightRules.calculateNextVelocity(
                Vec3.ZERO,
                new Vec3(0.0D, 1.0D, 0.0D),
                0.2D,
                1.0D
        );

        assertEquals(0.0D, next.x, EPSILON);
        assertEquals(0.2D, next.y, EPSILON);
        assertEquals(0.0D, next.z, EPSILON);
    }

    @Test
    void lookingDownTargetsDownwardFlight() {
        Vec3 next = IsaacFlightRules.calculateNextVelocity(
                Vec3.ZERO,
                new Vec3(0.0D, -1.0D, 0.0D),
                0.2D,
                1.0D
        );

        assertEquals(-0.2D, next.y, EPSILON);
    }
}
