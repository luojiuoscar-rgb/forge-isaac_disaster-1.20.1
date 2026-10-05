package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SphericalRandomAttackPatternTest {
    @Test
    void samplesUnitDirectionsAcrossBothHemispheres() {
        RandomSource random = RandomSource.create(531);
        boolean above = false;
        boolean below = false;
        boolean east = false;
        boolean west = false;

        for (int i = 0; i < 1_000; i++) {
            Vec3 direction = SphericalRandomAttackPattern.randomDirection(random);
            assertEquals(1.0, direction.lengthSqr(), 1.0E-12);
            above |= direction.y > 0.25;
            below |= direction.y < -0.25;
            east |= direction.x > 0.25;
            west |= direction.x < -0.25;
        }

        assertTrue(above && below && east && west);
    }
}
