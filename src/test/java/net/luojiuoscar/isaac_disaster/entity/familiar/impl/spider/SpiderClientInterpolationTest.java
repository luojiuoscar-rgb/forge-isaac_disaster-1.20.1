package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpiderClientInterpolationTest {
    @Test
    void nearbyAbsoluteGroundCorrectionsAdvanceOverAllInterpolationTicks() {
        for (double height : new double[]{-1.0 / 16, 1.0 / 16, -0.5, -1.5}) {
            var interpolation = new AbstractSpiderFamiliarEntity.ClientInterpolation();
            assertFalse(interpolation.accept(Vec3.ZERO, 0.5, height, 0, 3, true),
                    "An absolute packet at a small height transition must not snap");
            assertPosition(interpolation, 0, 0, 0);
            for (int tick = 1; tick <= 3; tick++) {
                assertTrue(interpolation.tick());
                assertPosition(interpolation, 0.5 * tick / 3, height * tick / 3, 0);
            }
            assertFalse(interpolation.tick());
        }
    }

    @Test
    void anotherAbsolutePacketRetargetsFromTheCurrentPositionWithoutJumping() {
        var interpolation = new AbstractSpiderFamiliarEntity.ClientInterpolation();
        assertFalse(interpolation.accept(Vec3.ZERO, 0.6, -0.06, 0, 3, true));
        interpolation.tick();
        assertPosition(interpolation, 0.2, -0.02, 0);
        assertFalse(interpolation.accept(new Vec3(interpolation.x, interpolation.y, interpolation.z),
                0.8, -0.0625, 0, 3, true));
        assertPosition(interpolation, 0.2, -0.02, 0);
        interpolation.tick();
        assertPosition(interpolation, 0.4, -0.0341666666667, 0);
        interpolation.tick(); interpolation.tick();
        assertPosition(interpolation, 0.8, -0.0625, 0);
    }

    @Test
    void farRecallSnapsAndCancelsAnyOldInterpolationTarget() {
        var interpolation = new AbstractSpiderFamiliarEntity.ClientInterpolation();
        assertFalse(interpolation.accept(Vec3.ZERO, 0.6, 0, 0, 3, false));
        interpolation.tick();
        assertTrue(interpolation.accept(new Vec3(interpolation.x, 0, 0), 20, 2, -3, 3, true));
        assertPosition(interpolation, 20, 2, -3);
        assertFalse(interpolation.tick(), "The old nearby target cannot drag a recalled spider back");
    }

    @Test
    void relativePacketsAndZeroStepUpdatesStillConverge() {
        var interpolation = new AbstractSpiderFamiliarEntity.ClientInterpolation();
        assertFalse(interpolation.tick());
        assertFalse(interpolation.accept(new Vec3(2, 3, 4), 2.3, 3, 4.3, 0, false));
        assertPosition(interpolation, 2, 3, 4);
        assertTrue(interpolation.tick());
        assertPosition(interpolation, 2.3, 3, 4.3);
        assertFalse(interpolation.tick());
    }

    private static void assertPosition(AbstractSpiderFamiliarEntity.ClientInterpolation interpolation,
                                       double x, double y, double z) {
        assertEquals(x, interpolation.x, 1e-9);
        assertEquals(y, interpolation.y, 1e-9);
        assertEquals(z, interpolation.z, 1e-9);
    }
}
