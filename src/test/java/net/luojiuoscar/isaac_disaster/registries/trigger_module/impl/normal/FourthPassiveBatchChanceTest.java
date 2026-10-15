package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FourthPassiveBatchChanceTest {
    @Test
    void appleChanceUsesFlooredLuckAndCapsAtOne() {
        assertEquals(1.0 / 15, Apple.chance(0), 1e-12);
        assertEquals(1.0 / 15, Apple.chance(0.9), 1e-12);
        assertEquals(1.0 / 16, Apple.chance(-0.1), 1e-12);
        assertEquals(0.5, Apple.chance(13), 1e-12);
        assertEquals(1, Apple.chance(14), 1e-12);
        assertEquals(1, Apple.chance(100), 1e-12);
    }
}
