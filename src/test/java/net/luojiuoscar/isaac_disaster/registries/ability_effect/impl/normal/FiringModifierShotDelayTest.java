package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FiringModifierShotDelayTest {
    @Test
    void haemolacriaDoublesAndAddsElevenToTheResolvedDelayOnce() {
        assertEquals(31.0, FiringModifierShotDelay.computeShotDelay(
                10.0, false, false, false, false, false, true));
        assertEquals(51.0, FiringModifierShotDelay.computeShotDelay(
                10.0, true, false, false, false, false, true));
        assertEquals(71.0, FiringModifierShotDelay.computeShotDelay(
                10.0, false, false, false, false, true, true));
        assertEquals(31.0, FiringModifierShotDelay.computeShotDelay(
                10.0, true, false, false, true, false, true));
        assertEquals(10.0, FiringModifierShotDelay.computeShotDelay(
                10.0, false, false, false, false, false, false));
    }
}
