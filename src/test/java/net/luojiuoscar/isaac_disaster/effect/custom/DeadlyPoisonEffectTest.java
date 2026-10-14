package net.luojiuoscar.isaac_disaster.effect.custom;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeadlyPoisonEffectTest {
    @Test
    void levelsDefineDamageWithoutASeparateSource() {
        assertEquals(1, DeadlyPoisonEffect.damageForAmplifier(0));
        assertEquals(1.5, DeadlyPoisonEffect.damageForAmplifier(1));
        assertEquals(3, DeadlyPoisonEffect.damageForAmplifier(4));
        assertEquals(128.5, DeadlyPoisonEffect.damageForAmplifier(255));
    }

    @Test
    void snapshotsRoundToTheNearestHalfPointWithTiesUp() {
        assertEquals(0, DeadlyPoisonEffect.amplifierForDamage(1.24));
        assertEquals(1, DeadlyPoisonEffect.amplifierForDamage(1.25));
        assertEquals(2, DeadlyPoisonEffect.amplifierForDamage(2));
        assertEquals(14, DeadlyPoisonEffect.amplifierForDamage(8.2));
        assertEquals(15, DeadlyPoisonEffect.amplifierForDamage(8.3));
    }

    @Test
    void snapshotsRespectThePotionBounds() {
        assertEquals(0, DeadlyPoisonEffect.amplifierForDamage(-100));
        assertEquals(0, DeadlyPoisonEffect.amplifierForDamage(0));
        assertEquals(255, DeadlyPoisonEffect.amplifierForDamage(1e100));
        assertEquals(255, DeadlyPoisonEffect.amplifierForDamage(Double.POSITIVE_INFINITY));
        assertEquals(0, DeadlyPoisonEffect.amplifierForDamage(Double.NaN));
    }
}
