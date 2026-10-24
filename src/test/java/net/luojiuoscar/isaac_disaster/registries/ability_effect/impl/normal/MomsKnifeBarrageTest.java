package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MomsKnifeBarrageTest {
    @Test
    void tappingStartsAtTwoAndChargeUnlocksEachAdditionalKnife() {
        assertEquals(2, MomsKnifeBarrage.countForCharge(0));
        assertEquals(2, MomsKnifeBarrage.countForCharge(0.124));
        assertEquals(6, MomsKnifeBarrage.countForCharge(0.5));
        for (int step = 1; step <= 8; step++) {
            assertEquals(2 + step, MomsKnifeBarrage.countForCharge(step / 8.0));
            assertEquals(1 + step, MomsKnifeBarrage.countForCharge(step / 8.0 - 1e-6));
        }
    }

    @Test
    void invalidChargeFractionsAreNormalizedToTheSafeRange() {
        assertEquals(2, MomsKnifeBarrage.countForCharge(-1));
        assertEquals(10, MomsKnifeBarrage.countForCharge(2));
        assertEquals(2, MomsKnifeBarrage.countForCharge(Double.NaN));
        assertEquals(2, MomsKnifeBarrage.countForCharge(Double.NEGATIVE_INFINITY));
        assertEquals(2, MomsKnifeBarrage.countForCharge(Double.POSITIVE_INFINITY));
    }
}
