package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MomKnifeTargetingTest {
    @Test
    void oneLayerUsesASphericalFiveAndAHalfBlockRange() {
        Vec3 owner = new Vec3(10, 20, 30);
        double radius = MomKnifeTargeting.scanRadius(1);
        assertTrue(MomKnifeTargeting.inRearRange(owner, 0, owner.add(0, 0, -5.5), radius));
        assertFalse(MomKnifeTargeting.inRearRange(owner, 0, owner.add(0, 0, -5.51), radius));
        assertFalse(MomKnifeTargeting.inRearRange(owner, 0, owner.add(4.5, 0, -4.5), radius));
        assertTrue(MomKnifeTargeting.inRearRange(owner, 0, owner.add(0, 3, -4), radius));
        assertFalse(MomKnifeTargeting.inRearRange(owner, 0, owner.add(0, 4, -4), radius));
    }

    @Test
    void sectorKeepsTheExistingRearAngleAndRotatesWithTheOwner() {
        double radius = MomKnifeTargeting.scanRadius(1);
        assertTrue(MomKnifeTargeting.inRearRange(Vec3.ZERO, 0, new Vec3(0, 0, -2), radius));
        assertFalse(MomKnifeTargeting.inRearRange(Vec3.ZERO, 0, new Vec3(0, 0, 2), radius));
        assertFalse(MomKnifeTargeting.inRearRange(Vec3.ZERO, 0, new Vec3(2, 0, 0), radius));
        assertTrue(MomKnifeTargeting.inRearRange(Vec3.ZERO, 0, new Vec3(2, 0, -1), radius));
        assertFalse(MomKnifeTargeting.inRearRange(Vec3.ZERO, 0, new Vec3(2, 0, -0.1), radius));
        assertTrue(MomKnifeTargeting.inRearRange(Vec3.ZERO, 90, new Vec3(2, 0, 0), radius));
        assertFalse(MomKnifeTargeting.inRearRange(Vec3.ZERO, 90, new Vec3(-2, 0, 0), radius));
        assertFalse(MomKnifeTargeting.inRearRange(Vec3.ZERO, 0, new Vec3(0, 2, 0), radius));
    }

    @Test
    void searchCadenceAndLayerRadiusFollowTheKnifeCount() {
        assertEquals(5, MomKnifeTargeting.SCAN_INTERVAL);
        assertEquals(5.5, MomKnifeTargeting.scanRadius(1));
        assertEquals(6, MomKnifeTargeting.scanRadius(2));
        assertEquals(6.5, MomKnifeTargeting.scanRadius(3));
        assertEquals(5.5, MomKnifeTargeting.scanRadius(0));
    }
}
