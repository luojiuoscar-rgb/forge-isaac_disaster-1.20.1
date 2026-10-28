package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VirgoTest {
    @Test
    void luckPreservesBaseChanceAndReachesFiftyPercentOnlyAtTen() {
        assertEquals(0.25, Virgo.chance(0));
        assertEquals(0.25, Virgo.chance(4));
        assertEquals(1.0 / 3, Virgo.chance(5));
        assertEquals(1.0 / 3, Virgo.chance(7));
        assertEquals(1.0 / 3, Virgo.chance(9.999));
        assertEquals(0.5, Virgo.chance(10));
        assertEquals(0.5, Virgo.chance(100));
        assertEquals(0.2, Virgo.chance(-1));
    }
}
