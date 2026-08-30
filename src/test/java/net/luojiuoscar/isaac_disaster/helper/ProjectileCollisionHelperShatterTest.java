package net.luojiuoscar.isaac_disaster.helper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectileCollisionHelperShatterTest {
    @Test
    void fragmentCountUsesTwentyPercentOfThePreviousCurve() {
        assertEquals(2, ProjectileCollisionHelper.fragmentCount(1.0D));
        assertEquals(1, ProjectileCollisionHelper.fragmentCount(0.25D));
        assertEquals(5, ProjectileCollisionHelper.fragmentCount(100.0D));
    }

    @Test
    void invalidScaleFallsBackToTheMinimumSafeSize() {
        assertEquals(0.25D, ProjectileCollisionHelper.normalizeScale(Double.NaN));
        assertEquals(0.25D, ProjectileCollisionHelper.normalizeScale(Double.POSITIVE_INFINITY));
        assertEquals(0.0125F, ProjectileCollisionHelper.fragmentQuadSize(-2.0D));
    }

    @Test
    void collisionSizeMatchesTheRenderedFragmentDiameter() {
        assertEquals(0.1F, ProjectileCollisionHelper.fragmentCollisionSize(1.0D));
        assertEquals(0.025F, ProjectileCollisionHelper.fragmentCollisionSize(0.25D));
    }
}
