package net.luojiuoscar.isaac_disaster.groundeffect;

import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContactGeometry;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.WaterPuddleEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class GroundEffectAnimationTest {
    private final GroundEffectType<WaterPuddleEffect> water = new GroundEffectType<>(WaterPuddleEffect::new);

    @Test void animationDurationNormalizesNegativeValuesAndZeroSkipsGrowth() {
        for (int duration : new int[]{-10, 0}) {
            var effect = water.create(0, 1, null, Vec3.ZERO, 1, 100, 1, 0, duration);
            assertEquals(0, effect.animationDurationTicks());
            assertEquals(1, effect.currentRadius());
            assertTrue(effect.isSettled());
        }
    }

    @Test void contactsGrowWithTheOutlineWhileHeightAndIndexRadiusStayFixed() {
        var effect = water.create(0, 1, null, Vec3.ZERO, 2, 100, 1, 0, 10);
        var target = new AABB(0.75, 0, -0.1, 0.85, 1.8, 0.1);
        assertEquals(2, effect.radius());
        assertEquals(0, effect.currentRadius());
        assertFalse(effect.contains(Vec3.ZERO));
        assertFalse(GroundEffectContactGeometry.touches(effect, target));
        for (int i = 0; i < 5; i++) effect.tick();
        assertEquals(1, effect.currentRadius());
        assertTrue(GroundEffectContactGeometry.touches(effect, target));
        assertFalse(effect.contains(new Vec3(1.5, 0, 0)));
        assertEquals(0, effect.position().y);
        for (int i = 0; i < 5; i++) effect.tick();
        assertEquals(2, effect.currentRadius());
        assertTrue(effect.contains(new Vec3(1.5, 0, 0)));
    }

    @Test void refreshKeepsIndependentAnimationProgressAndParametersAffectReuse() {
        var manager = new GroundEffectManager();
        var owner = new UUID(1, 2);
        var args = 10;
        var effect = manager.spawn(water, owner, Vec3.ZERO, 1, 100, 1, args);
        for (int i = 0; i < 5; i++) effect.tick();
        assertSame(effect, manager.spawn(water, owner, Vec3.ZERO, 1, 100, 1, 10));
        assertEquals(0, effect.age());
        assertEquals(5, effect.animationElapsedTicks());
        assertEquals(0.5, effect.currentRadius());
        var other = manager.spawn(water, owner, Vec3.ZERO, 1, 100, 1, 20);
        assertNotSame(effect, other);
        assertEquals(2, manager.activeCount());
        assertNotSame(effect, manager.spawn(water, owner, Vec3.ZERO, 1, 100, 1));
        assertSame(effect.footprint(), effect.coverage(), "Growing paint cannot be permanently clipped");
    }

    @Test void growingPaintNeitherErasesOldPaintNorClaimsCapacityFromFutureCoverage() {
        var manager = new GroundEffectManager();
        var old = manager.spawn(water, null, Vec3.ZERO, 0.5, 100, 1);
        var expanding = manager.spawn(water, null, Vec3.ZERO, 2, 100, 1, 10);
        assertEquals(2, manager.activeCount());
        assertTrue(old.contains(Vec3.ZERO));
        assertFalse(expanding.isSettled());
        assertTrue(old.isSettled());
        for (int i = 2; i < GroundEffectManager.MAX_ACTIVE_EFFECTS; i++)
            manager.spawn(water, null, new Vec3(i * 3, 0, 0), 0.5, 100, 1);
        assertNull(manager.spawn(water, null, Vec3.ZERO, 3, 100, 1, 10));
        assertTrue(old.contains(Vec3.ZERO));
    }

    @Test void immediatePaintRefreshKeepsItsFullSizeAndExpirationIncludesGrowth() {
        var manager = new GroundEffectManager();
        var instant = manager.spawn(water, null, Vec3.ZERO, 1, 100, 1, 0);
        instant.tick();
        assertSame(instant, manager.spawn(water, null, Vec3.ZERO, 1, 100, 1, 0));
        assertEquals(1, instant.currentRadius());
        var shortLived = water.create(1, 1, null, Vec3.ZERO, 1, 10, 1, 1, 10);
        for (int i = 0; i < 9; i++) assertTrue(shortLived.tick());
        assertFalse(shortLived.tick());
        assertTrue(shortLived.isExpired());
        assertFalse(shortLived.isSettled());
    }
}
