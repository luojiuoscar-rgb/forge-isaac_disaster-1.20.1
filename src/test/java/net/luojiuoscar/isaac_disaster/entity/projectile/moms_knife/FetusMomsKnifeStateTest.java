package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FetusMomsKnifeStateTest {
    @Test
    void bladeLengthRadiusAndForwardOffsetScaleWithTheFetus() {
        Vec3 forward = new Vec3(0, 0, 1);
        var blade = FetusMomsKnifeState.blade(Vec3.ZERO, forward, 1, 0.21);
        assertEquals(0.105, blade.anchor().z, 1e-12);
        assertEquals(0.39375, blade.tip().distanceTo(blade.anchor()), 1e-12);
        assertEquals(0.0375, blade.radius(), 1e-12);
        var large = FetusMomsKnifeState.blade(Vec3.ZERO, forward, 2, 0.42);
        assertEquals(0.7875, large.tip().distanceTo(large.anchor()), 1e-12);
        assertEquals(0.075, large.radius(), 1e-12);
        assertTrue(PlayerMomsKnifeMotion.contacts(blade, blade, new AABB(-0.01, -0.01, 0.3, 0.01, 0.01, 0.4)));
        assertFalse(PlayerMomsKnifeMotion.contacts(blade, blade, new AABB(0.05, -0.01, 0.3, 0.06, 0.01, 0.4)));
    }

    @Test
    void targetsAreDeduplicatedPerBladeAndTickRatherThanPerLifetime() {
        var first = new FetusMomsKnifeState(new Vec3(0, 0, 1));
        var second = new FetusMomsKnifeState(new Vec3(0, 0, 1));
        UUID target = new UUID(1, 1);
        assertTrue(first.markHit(10, target));
        assertFalse(first.markHit(10, target));
        assertTrue(second.markHit(10, target));
        assertTrue(first.markHit(11, target));
        assertTrue(first.markHit(11, new UUID(1, 2)));
    }

    @Test
    void oppositeAndVerticalTurnsKeepFiniteBladesAndStoppedFacing() {
        var knife = new FetusMomsKnifeState(new Vec3(0, 0, 1));
        for (Vec3 direction : new Vec3[]{new Vec3(0, 0, -1), new Vec3(0, 1, 0), new Vec3(0, -1, 0)}) {
            var path = knife.sweep(Vec3.ZERO, direction, direction, 1, 0.21);
            assertTrue(path.size() > 2);
            for (var blade : path) {
                assertTrue(Double.isFinite(blade.tip().lengthSqr()));
                assertEquals(0.39375, blade.tip().distanceTo(blade.anchor()), 1e-10);
            }
            assertEquals(direction, knife.facing(Vec3.ZERO));
        }
    }

    @Test
    void clientFacingRetainsTheLastObservedHeadingWhenVelocityStops() {
        var knife = new FetusMomsKnifeState(new Vec3(0, 0, 1));
        assertEquals(new Vec3(1, 0, 0), knife.facing(new Vec3(2, 0, 0)));
        assertEquals(new Vec3(1, 0, 0), knife.facing(Vec3.ZERO));
    }

    @Test
    void attachingAKnifeDoesNotChangeTheFetusesPhysicsOrLifetime() {
        Vec3 velocity = new Vec3(0.2, 0.1, 0.3);
        var armed = BulletState.builder().velocity(velocity).noGravity(true).lifetime(12).fetusKnife(true).build();
        var plain = BulletState.builder().velocity(velocity).noGravity(true).lifetime(12).build();
        assertNotNull(armed.fetusKnife());
        assertNull(plain.fetusKnife());
        for (int i = 0; i < 15; i++) {
            assertEquals(plain.tickPhysics(), armed.tickPhysics());
            assertEquals(plain.position(), armed.position());
            assertEquals(plain.velocity(), armed.velocity());
            assertEquals(plain.traveled(), armed.traveled());
        }
    }
}
