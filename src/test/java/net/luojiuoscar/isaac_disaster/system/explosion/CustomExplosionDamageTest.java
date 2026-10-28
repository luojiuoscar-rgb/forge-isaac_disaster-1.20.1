package net.luojiuoscar.isaac_disaster.system.explosion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomExplosionDamageTest {
    @Test
    void standardCurveRetainsIsaacBombDamageWithoutExceedingItsCenter() {
        assertEquals(60, CustomExplosion.standardDamage(60, 1));
        assertEquals(23.125, CustomExplosion.standardDamage(60, 0.5));
        assertEquals(1, CustomExplosion.standardDamage(60, 0));
        assertEquals(60, CustomExplosion.standardDamage(60, 2));
        assertEquals(0, CustomExplosion.standardDamage(0, 1));
        assertTrue(CustomExplosion.standardDamage(0.25F, 0.5) <= 0.25F);
    }

    @Test
    void gigaKeepsTheLegacyQuadraticCurve() {
        assertEquals(300, CustomExplosion.gigaDamage(300, 0, 10));
        assertEquals(75, CustomExplosion.gigaDamage(300, 5, 10));
        assertEquals(0, CustomExplosion.gigaDamage(300, 10, 10));
        // Legacy selection is an AABB, so corner candidates keep the existing formula.
        assertEquals(75, CustomExplosion.gigaDamage(300, 15, 10));
        assertEquals(0, CustomExplosion.gigaDamage(300, 0, 0));
    }
}
