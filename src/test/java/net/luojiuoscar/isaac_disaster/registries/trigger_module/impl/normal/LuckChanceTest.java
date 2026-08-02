package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LuckChanceTest {
    private static final double EPSILON = 1.0E-12;

    @Test
    void lokisHornsScalesWithLuck() {
        assertEquals(0.25, LuckTriggerChance.lokisHorns(0.0), EPSILON);
        assertEquals(0.50, LuckTriggerChance.lokisHorns(5.0), EPSILON);
    }

    @Test
    void momsEyeshadowScalesWithLuck() {
        assertEquals(0.10, LuckTriggerChance.momsEyeshadow(0.0), EPSILON);
        assertEquals(1.0 / 9.0, LuckTriggerChance.momsEyeshadow(3.0), EPSILON);
    }

    @Test
    void commonColdScalesWithLuck() {
        assertEquals(0.25, LuckTriggerChance.commonCold(0.0), EPSILON);
        assertEquals(1.0 / 3.0, LuckTriggerChance.commonCold(4.0), EPSILON);
    }

    @Test
    void ironBarScalesWithLuck() {
        assertEquals(0.10, LuckTriggerChance.ironBar(0.0), EPSILON);
        assertEquals(1.0 / 9.0, LuckTriggerChance.ironBar(3.0), EPSILON);
    }
}
