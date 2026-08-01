package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractFromBelowTest {
    private static final double EPSILON = 1.0E-12;

    @Test
    void doesNotChangeLootWithoutTheItem() {
        assertEquals(5, ContractFromBelow.calculateStackCount(5, 0, 64));
    }

    @Test
    void multipliesStackCountAndCapsAtTheItemLimit() {
        assertEquals(4, ContractFromBelow.calculateStackCount(2, 1, 64));
        assertEquals(64, ContractFromBelow.calculateStackCount(30, 2, 64));
    }

    @Test
    void leavesNonStackableItemsUnchanged() {
        assertEquals(1, ContractFromBelow.calculateStackCount(1, 4, 1));
    }

    @Test
    void usesTheConfiguredCancellationFormula() {
        assertEquals(0.5 * Math.pow(0.666, 1),
                ContractFromBelow.getCancellationChance(1), EPSILON);
        assertEquals(0.5 * Math.pow(0.666, 2),
                ContractFromBelow.getCancellationChance(2), EPSILON);
    }
}
