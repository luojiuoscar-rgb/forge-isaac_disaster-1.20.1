package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class D8Test {
    @Test
    void samplesStayBoundedAndPreserveProductAcrossManyRolls() {
        Random random = new Random(406);
        int twoUp = 0;
        int threeUp = 0;
        int oneUp = 0;
        for (int roll = 0; roll < 20_000; roll++) {
            double product = 1;
            int increases = 0;
            for (double multiplier : D8.sampleMultipliers(random::nextDouble)) {
                assertTrue(multiplier >= 0.5 && multiplier <= 2);
                product *= multiplier;
                if (multiplier > 1) increases++;
            }
            assertEquals(1, product, 1e-12);
            if (increases == 2) twoUp++;
            if (increases == 3) threeUp++;
            if (increases == 1) oneUp++;
        }
        assertTrue(twoUp > threeUp && twoUp > oneUp);
        assertTrue(threeUp > 0 && oneUp > 0);
    }

    @Test
    void constantInputRemainsNeutralRatherThanAccumulatingScaling() {
        assertArrayEquals(new double[]{1, 1, 1, 1}, D8.sampleMultipliers(() -> 0.5));
    }

    @Test
    void rejectedDrawsRetryThenFallBackWithoutHanging() {
        int[] draws = {0};
        double[] result = D8.sampleMultipliers(() -> ++draws[0] % 4 == 0 ? 0.0 : Math.nextDown(1.0));
        assertEquals(64 * 4, draws[0]);
        assertArrayEquals(new double[]{1, 1, 1, 1}, result);
    }
}
