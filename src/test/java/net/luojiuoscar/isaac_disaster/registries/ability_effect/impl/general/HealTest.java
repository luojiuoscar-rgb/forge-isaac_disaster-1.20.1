package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealTest {
    private static final double EPSILON = 1.0E-12;

    @Test
    void doublesRedHeartPickupHealingForMaggysBowOwners() {
        assertEquals(1.0, Heal.getHealingAmplifier(0.5, true, true), EPSILON);
        assertEquals(2.0, Heal.getHealingAmplifier(1.0, true, true), EPSILON);
        assertEquals(4.0, Heal.getHealingAmplifier(2.0, true, true), EPSILON);
    }

    @Test
    void leavesNonRedHeartHealingAndNonOwnersUnchanged() {
        assertEquals(0.5, Heal.getHealingAmplifier(0.5, false, true), EPSILON);
        assertEquals(0.5, Heal.getHealingAmplifier(0.5, true, false), EPSILON);
    }
}
