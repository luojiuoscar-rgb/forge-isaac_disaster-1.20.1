package net.luojiuoscar.isaac_disaster.entity.familiar.impl.brother_bobby;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BrotherBobbyCombatTest {
    @Test void damageUsesConfiguredBonusAndEnhancementWithNonnegativeBoundary() {
        assertEquals(12, BrotherBobbyCombat.damage(2, 2));
        assertEquals(0, BrotherBobbyCombat.damage(2, -2));
        assertEquals(0, BrotherBobbyCombat.damage(-2, 1));
        assertEquals(0, BrotherBobbyCombat.damage(Double.NaN, 1));
    }
    @Test void rangeKeepsFloorAndDoesNotCapAtSixtyFour() {
        assertEquals(30, BrotherBobbyCombat.range(20));
        assertEquals(12, BrotherBobbyCombat.range(3));
        assertEquals(150, BrotherBobbyCombat.range(100));
    }
}
