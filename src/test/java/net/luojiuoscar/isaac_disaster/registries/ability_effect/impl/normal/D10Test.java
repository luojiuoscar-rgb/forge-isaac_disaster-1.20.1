package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class D10Test {
    @Test
    void rangeIsAnInclusiveThreeDimensionalSphere() {
        Vec3 center = new Vec3(10, 20, 30);
        assertTrue(D10.inRange(center, center.add(3, 4, 0), 5));
        assertTrue(D10.inRange(center, center.add(0, 5, 0), 5));
        assertFalse(D10.inRange(center, center.add(4, 4, 0), 5));
        assertFalse(D10.inRange(center, center.add(0, 5.001, 0), 5));
        assertTrue(D10.inRange(center, center, 0));
    }

    @Test
    void healthUsesTheInitializedNewMaximumAndClampsTheOldRatio() {
        assertEquals(40, D10.replacementHealth(5, 20, 160));
        assertEquals(8, D10.replacementHealth(20, 20, 8));
        assertEquals(8, D10.replacementHealth(100, 20, 8));
        assertEquals(0, D10.replacementHealth(-1, 20, 8));
    }
}
