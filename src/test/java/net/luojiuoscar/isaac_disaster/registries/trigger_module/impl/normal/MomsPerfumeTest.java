package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MomsPerfumeTest {
    private static final double EPSILON = 1.0E-12;

    @Test
    void startsAtFifteenPercentWithZeroLuck() {
        assertEquals(0.15, MomsPerfume.getTriggerChance(0.0), EPSILON);
    }

    @Test
    void reachesFullChanceAtEightyFiveLuck() {
        assertEquals(1.0, MomsPerfume.getTriggerChance(85.0), EPSILON);
    }

    @Test
    void neverExceedsFullChance() {
        assertEquals(1.0, MomsPerfume.getTriggerChance(120.0), EPSILON);
    }
}
