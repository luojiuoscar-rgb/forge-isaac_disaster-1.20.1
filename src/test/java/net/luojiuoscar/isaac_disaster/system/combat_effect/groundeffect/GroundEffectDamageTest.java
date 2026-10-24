package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.manager.ColorManager;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.GroundEffectTestFixtures.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GroundEffectDamageTest {
    @Test
    void waterStoresTheExplicitSpawnDamageAndNormalizesInvalidValues() {
        var water = WATER.create(0, 1, null, Vec3.ZERO, 0.5D, 100, 3.5F, 0);
        assertEquals(3.5F, water.damage());
        for (float invalid : new float[]{-1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            var harmless = WATER.create(0, 1, null, Vec3.ZERO, 0.5D, 100, invalid, 0);
            assertEquals(0.0F, harmless.damage());
        }
    }

    @Test
    void lemonInstancesShareATypeWhileKeepingTheirOwnDamageAndYellowColor() {
        var manager = new GroundEffectManager();
        var owner = new UUID(1, 2);
        var small = manager.spawn(LEMON, owner, Vec3.ZERO, 1, 100, 2.5F);
        var large = manager.spawn(LEMON, owner, new Vec3(5, 0, 0), 2, 100, 4.0F);
        assertNotNull(small);
        assertNotNull(large);
        assertSame(small.getType(), large.getType());
        assertEquals(2.5F, small.damage());
        assertEquals(4.0F, large.damage());
        assertEquals(ColorManager.LEMON, small.getColor());
        assertNotSame(WATER, small.getType());
    }

    @Test
    void repaintWithIdenticalDamageReusesTheInstanceButChangedDamagePaintsANewSnapshot() {
        var manager = new GroundEffectManager();
        var owner = new UUID(1, 2);
        var first = water(manager, owner, Vec3.ZERO, 0.5D, 100, 8.0F);
        assertSame(first, water(manager, owner, Vec3.ZERO, 0.5D, 100, 8.0F));
        var weaker = water(manager, owner, Vec3.ZERO, 0.5D, 100, 2.0F);
        assertNotNull(weaker);
        assertNotSame(first, weaker);
        assertEquals(2.0F, weaker.damage(), "A later weaker puddle keeps its own damage");
        assertEquals(8.0F, first.damage(), "The original instance keeps its original snapshot");
        assertTrue(first.contains(Vec3.ZERO), "Incomplete coverage leaves the previous outline intact");
        assertTrue(weaker.contains(Vec3.ZERO));
    }
}
