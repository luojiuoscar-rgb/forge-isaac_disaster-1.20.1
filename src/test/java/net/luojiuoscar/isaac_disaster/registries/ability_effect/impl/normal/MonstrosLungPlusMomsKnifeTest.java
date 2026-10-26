package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonstrosLungPlusMomsKnifeTest {
    @Test
    void lungUnlocksFourTiersAtEachQuarterCharge() {
        assertEquals(1, MonstrosLungPlusMomsKnife.countForCharge(0));
        assertEquals(1, MonstrosLungPlusMomsKnife.countForCharge(0.25 - 1e-6));
        assertEquals(2, MonstrosLungPlusMomsKnife.countForCharge(0.25));
        assertEquals(2, MonstrosLungPlusMomsKnife.countForCharge(0.5 - 1e-6));
        assertEquals(3, MonstrosLungPlusMomsKnife.countForCharge(0.5));
        assertEquals(3, MonstrosLungPlusMomsKnife.countForCharge(0.75 - 1e-6));
        assertEquals(4, MonstrosLungPlusMomsKnife.countForCharge(0.75));
        assertEquals(4, MonstrosLungPlusMomsKnife.countForCharge(1));
    }

    @Test
    void chargeInputsAreClampedWithoutCreatingExcessKnives() {
        assertEquals(1, MonstrosLungPlusMomsKnife.countForCharge(-1));
        assertEquals(4, MonstrosLungPlusMomsKnife.countForCharge(2));
        assertEquals(1, MonstrosLungPlusMomsKnife.countForCharge(Double.NaN));
        assertEquals(1, MonstrosLungPlusMomsKnife.countForCharge(Double.NEGATIVE_INFINITY));
        assertEquals(1, MonstrosLungPlusMomsKnife.countForCharge(Double.POSITIVE_INFINITY));
    }
}
