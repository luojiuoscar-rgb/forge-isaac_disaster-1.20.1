package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.Infestation;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.SpiderBaby;

import static org.junit.jupiter.api.Assertions.*;

class SwarmGenerationChanceTest {
    @Test
    void infestationHasItsOwnLuckCurveAndInputBounds() {
        assertEquals(0.1, Infestation.chance(0));
        assertEquals(0.06, Infestation.chance(-2), 1e-12);
        assertEquals(0, Infestation.chance(-5));
        assertEquals(0, Infestation.chance(-Double.MAX_VALUE));
        assertEquals(0.125, Infestation.chance(1.25), 1e-12);
        assertEquals(0.25, Infestation.chance(7.5));
        assertEquals(0.25, Infestation.chance(Double.MAX_VALUE));
        for (double value : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            assertEquals(0.1, Infestation.chance(value));
        }
    }

    @Test
    void spiderBabyHasItsOwnLuckCurveAndInputBounds() {
        assertEquals(0.1, SpiderBaby.chance(0));
        assertEquals(0.06, SpiderBaby.chance(-2), 1e-12);
        assertEquals(0, SpiderBaby.chance(-5));
        assertEquals(0, SpiderBaby.chance(-Double.MAX_VALUE));
        assertEquals(0.125, SpiderBaby.chance(1.25), 1e-12);
        assertEquals(0.25, SpiderBaby.chance(7.5));
        assertEquals(0.25, SpiderBaby.chance(Double.MAX_VALUE));
        for (double value : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            assertEquals(0.1, SpiderBaby.chance(value));
        }
    }

    @Test
    void wigUsesUnscaledLuckIncludingNegativeLuck() {
        assertEquals(0.05, MomsWig.chance(0));
        assertEquals(0.025, MomsWig.chance(-10));
        assertEquals(1.0 / 18, MomsWig.chance(1));
    }

    @Test
    void wigFloorsTwiceLuckAndReachesCertaintyAtNinePointFive() {
        assertEquals(0.5, MomsWig.chance(9.49));
        assertEquals(1, MomsWig.chance(9.5));
        assertEquals(1, MomsWig.chance(10));
        assertEquals(1, MomsWig.chance(Double.MAX_VALUE));
    }

    @Test
    void allFiniteLuckKeepsAProbabilityInRangeWithoutDroppingAtTheThreshold() {
        double previous = 0;
        for (int hundredths = -10000; hundredths <= 10000; hundredths++) {
            double chance = MomsWig.chance(hundredths / 100.0);
            assertTrue(chance >= previous && chance <= 1);
            previous = chance;
        }
    }

    @Test
    void malformedLuckFallsBackToBaseChance() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0.05, MomsWig.chance(value));
        }
    }

    @Test
    void hiveMindChangesOnlyTheDedicatedWigThresholdAndMulliganStaysIndependent() {
        assertEquals(5, MomsWig.generationLimit(false));
        assertEquals(10, MomsWig.generationLimit(true));
        assertEquals(30, TheMulligan.GENERATION_LIMIT);
        assertEquals(1.0 / 6, TheMulligan.chance());
    }
}
