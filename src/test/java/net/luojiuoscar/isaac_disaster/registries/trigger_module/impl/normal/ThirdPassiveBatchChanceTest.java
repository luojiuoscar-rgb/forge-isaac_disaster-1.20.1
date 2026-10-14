package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThirdPassiveBatchChanceTest {
    @Test
    void contactsUsesFloorStepsAndNeverExceedsHalf() {
        assertEquals(0.2, MomsContacts.chance(0), 1e-12);
        assertEquals(1.0/6, MomsContacts.chance(-0.1), 1e-12);
        assertEquals(0.2, MomsContacts.chance(6), 1e-12);
        assertEquals(0.25, MomsContacts.chance(7), 1e-12);
        assertEquals(0.5, MomsContacts.chance(20), 1e-12);
        assertEquals(0.5, MomsContacts.chance(100), 1e-12);
    }

    @Test
    void darkMatterUsesItsOwnLuckCurve() {
        assertEquals(1.0/3, DarkMatter.chance(0), 1e-12);
        assertEquals(0.25, DarkMatter.chance(-0.1), 1e-12);
        assertEquals(0.5, DarkMatter.chance(10), 1e-12);
        assertEquals(1, DarkMatter.chance(20), 1e-12);
        assertEquals(1, DarkMatter.chance(100), 1e-12);
    }

    @Test
    void celticCrossAdaptationHasDocumentedEndpointsAndIsBounded() {
        assertEquals(0.2, CelticCross.chance(0), 1e-12);
        assertEquals(1, CelticCross.chance(27), 1e-12);
        assertEquals(1, CelticCross.chance(100), 1e-12);
        assertEquals(0, CelticCross.chance(-100), 1e-12);
        assertTrue(CelticCross.chance(10) > CelticCross.chance(0));
    }

    @Test
    void bandageUsesRepentanceLinearChanceAndMarblesIgnoresLuck() {
        assertEquals(0.2, OldBandage.chance(0), 1e-12);
        assertEquals(0, OldBandage.chance(-20), 1e-12);
        assertEquals(1, OldBandage.chance(80), 1e-12);
        assertEquals(1, OldBandage.chance(100), 1e-12);
        assertEquals(0.1, Marbles.chance(-100), 1e-12);
        assertEquals(0.1, Marbles.chance(100), 1e-12);
    }
}
