package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.WaterPuddleEffect;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.GroundEffectTestFixtures.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundEffectStateTest {
    @Test
    void waterPuddleUsesConfiguredLifetimeAndExpiresAtTheBoundary() {
        var effect = WATER.create(3, 2, null, Vec3.ZERO, WaterPuddleEffect.DEFAULT_RADIUS, WaterPuddleEffect.DEFAULT_LIFETIME, 1.0F, 11L);

        for (int i = 0; i < WaterPuddleEffect.DEFAULT_LIFETIME - 1; i++) {
            assertTrue(effect.tick());
        }
        assertFalse(effect.isExpired());
        assertFalse(effect.tick());
        assertTrue(effect.isExpired());
        assertEquals(WaterPuddleEffect.DEFAULT_LIFETIME, effect.age());
    }

    @Test
    void coverageUsesTheSharedHorizontalBrushOutline() {
        var effect = WATER.create(0, 0, null, new Vec3(4, 12, -2), WaterPuddleEffect.DEFAULT_RADIUS, WaterPuddleEffect.DEFAULT_LIFETIME, 1.0F, 1L);

        assertTrue(effect.contains(new Vec3(4.4, 100, -2)));
        assertTrue(effect.contains(new Vec3(4, -50, -2.4)));
        assertEquals(0.75D, effect.radius());
        assertTrue(effect.contains(new Vec3(4.6, 12, -2)));
        assertFalse(effect.contains(new Vec3(4 + WaterPuddleEffect.DEFAULT_RADIUS + 0.01, 12, -2)));
    }

    @Test
    void identityCombinesSlotAndGeneration() {
        var effect = WATER.create(7, 4, null, Vec3.ZERO, WaterPuddleEffect.DEFAULT_RADIUS, WaterPuddleEffect.DEFAULT_LIFETIME, 1.0F, 1L);

        assertEquals((4L << 32) | 7L, effect.identity());
    }
}
