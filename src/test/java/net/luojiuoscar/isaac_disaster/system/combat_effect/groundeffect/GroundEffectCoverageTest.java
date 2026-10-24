package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core.GroundEffectCoverage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroundEffectCoverageTest {
    @Test
    void completeContainmentIncludesEveryTranslatedVertex() {
        var large = GroundEffectCoverage.puddle(2, 1);
        var small = GroundEffectCoverage.puddle(0.5, 2);
        assertTrue(large.covers(small, 0.5, 0.25));
        assertFalse(small.covers(large, 0, 0));
        assertFalse(large.covers(small, 2, 0));
        assertTrue(large.covers(large, 0, 0));
        assertFalse(large.contains(Double.NaN, 0));
        assertFalse(large.covers(small, Double.POSITIVE_INFINITY, 0));
        assertTrue(GroundEffectCoverage.puddle(1, 9).covers(GroundEffectCoverage.puddle(0.5, 0), 0.501, 0),
                "Actual outlines can fully overlap even when their nominal enclosing circles do not");
    }

    @Test
    void partialOverlapLeavesBothOriginalOutlinesUnchanged() {
        var first = GroundEffectCoverage.puddle(0.5, 1);
        var second = GroundEffectCoverage.puddle(0.5, 2);
        assertFalse(first.covers(second, 0.4, 0));
        assertFalse(second.covers(first, -0.4, 0));
        assertTrue(first.contains(0.2, 0));
        assertTrue(second.contains(-0.2, 0));
        assertEquals(1, first.pieces().size());
        assertEquals(12, first.pieces().get(0).vertices().size());
    }

    @Test
    void radiusAndSeedRecreateTheSameOutlineWithoutGeometryOnTheWire() {
        assertEquals(GroundEffectCoverage.puddle(1.5, 123).pieces(),
                GroundEffectCoverage.puddle(1.5, 123).pieces());
        for (double radius : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            var empty = GroundEffectCoverage.puddle(radius, 1);
            assertTrue(empty.isEmpty());
            assertFalse(empty.contains(0, 0));
        }
    }

    @Test
    void everyGeneratedOutlineIsConvex() {
        for (int seed = 0; seed < 256; seed++) {
            var points = GroundEffectCoverage.puddle(0.5, seed).pieces().get(0).vertices();
            for (int i = 0; i < points.size(); i++) {
                var a = points.get(i);
                var b = points.get((i + 1) % points.size());
                var c = points.get((i + 2) % points.size());
                assertTrue((b.x() - a.x()) * (c.z() - b.z()) - (b.z() - a.z()) * (c.x() - b.x()) > 0);
            }
        }
    }
}
