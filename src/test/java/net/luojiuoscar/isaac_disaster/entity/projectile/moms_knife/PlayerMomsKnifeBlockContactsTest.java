package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeBlockContactsTest {
    @Test
    void stationaryBladeReportsAContactEvenWhenItsHandleIsOutside() {
        var blade = PlayerMomsKnifeMotion.blade(Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD);
        var hit = PlayerMomsKnifeBlockContacts.contact(blade, blade, new AABB(-0.02, -0.02, 0.5, 0.02, 0.02, 0.6));
        assertNotNull(hit);
        assertEquals(0, hit.fraction());
    }

    @Test
    void aFastSweepFindsTheFirstBladeContactAndItsSurfaceNormal() {
        var from = PlayerMomsKnifeMotion.blade(Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD);
        var to = PlayerMomsKnifeMotion.blade(new Vec3(0, 0, 10), PlayerMomsKnifeMotion.FORWARD);
        var hit = PlayerMomsKnifeBlockContacts.contact(from, to, new AABB(-0.1, -0.1, 4, 0.1, 0.1, 5));
        assertNotNull(hit);
        assertEquals((4 - 0.08 - 0.84) / 10, hit.fraction(), 1e-10);
        assertEquals(Direction.NORTH, hit.face());
        assertEquals(3.92, hit.point().z, 1e-10);
    }

    @Test
    void aRotationCanSweepTheMiddleOfTheBladeAcrossAThinBox() {
        var from = PlayerMomsKnifeMotion.blade(Vec3.ZERO, new Vec3(1, 0, 0));
        var to = PlayerMomsKnifeMotion.blade(Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD);
        assertNotNull(PlayerMomsKnifeBlockContacts.contact(from, to, new AABB(0.28, -0.02, 0.28, 0.32, 0.02, 0.32)));
    }

    @Test
    void collisionShapesDoNotExpandToAFullBlockAndTheFinalTipStillCounts() {
        var from = PlayerMomsKnifeMotion.blade(new Vec3(0, 0.8, 0), PlayerMomsKnifeMotion.FORWARD);
        var to = PlayerMomsKnifeMotion.blade(new Vec3(0, 0.8, 2), PlayerMomsKnifeMotion.FORWARD);
        assertNull(PlayerMomsKnifeBlockContacts.contact(from, to, new AABB(-0.5, 0, 1, 0.5, 0.5, 2)));
        assertNotNull(PlayerMomsKnifeBlockContacts.contact(from, to, new AABB(-0.1, 0.75, 2.8, 0.1, 0.85, 2.9)));
        assertNull(PlayerMomsKnifeBlockContacts.contact(from, to, new AABB(1, 0.75, 1, 2, 0.85, 2)));
    }
}
