package net.luojiuoscar.isaac_disaster.helper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerHelperFireRateTest {
    @Test
    void preservesDefaultAndNegativeTearsDelay() {
        assertEquals(20.0 / 7.0, PlayerHelper.tearsToFireRate(0.0), 1.0e-12);
        assertEquals(20.0 / 15.0, PlayerHelper.tearsToFireRate(-1.0), 1.0e-12);
        assertEquals(20.0 / 39.0, PlayerHelper.tearsToFireRate(-7.0), 1.0e-12);
        assertEquals(PlayerHelper.tearsToFireRate(-7.0), PlayerHelper.tearsToFireRate(-100.0));
    }

    @Test
    void onlyTearsConversionHasTheFiveShotsPerSecondCap() {
        assertEquals(5.0, PlayerHelper.tearsToFireRate(165.0 / 104.0));
        assertEquals(5.0, PlayerHelper.tearsToFireRate(100.0));
        assertTrue(PlayerHelper.tearsToFireRate(1.0) < 5.0);
    }

    @Test
    void allFormulaBranchesIncreaseContinuously() {
        double boundary = -10.0 / 13.0;
        assertEquals(PlayerHelper.tearsToFireRate(boundary - 1.0e-10),
                PlayerHelper.tearsToFireRate(boundary + 1.0e-10), 1.0e-4);
        double previous = PlayerHelper.tearsToFireRate(-7.0);
        for (int i = -699; i <= 200; i++) {
            double current = PlayerHelper.tearsToFireRate(i / 100.0);
            assertTrue(current >= previous);
            previous = current;
        }
    }
}
