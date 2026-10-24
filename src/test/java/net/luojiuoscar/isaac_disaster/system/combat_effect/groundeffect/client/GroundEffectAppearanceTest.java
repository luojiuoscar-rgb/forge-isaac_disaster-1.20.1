package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundEffectAppearanceTest {
    @Test
    void hostileBrightnessRisesAndFallsOverHalfASecond() {
        double[] times = {0, 0.125, 0.25, 0.375, 0.5};
        float[] expected = {1.5F, 1.125F, 0.75F, 1.125F, 1.5F};
        for (int i = 0; i < times.length; i++) {
            assertEquals(expected[i], GroundEffectAppearance.brightness(true, times[i]), 1.0E-5F);
            assertEquals(expected[i], GroundEffectAppearance.brightness(true, times[i] + 0.5), 1.0E-5F);
        }
    }

    @Test
    void brightnessIsContinuousBetweenFramesAndAcrossCycleBoundaries() {
        float previous = GroundEffectAppearance.brightness(true, 0);
        for (int frame = 1; frame <= 1000; frame++) {
            float current = GroundEffectAppearance.brightness(true, frame / 1000.0);
            assertTrue(current >= 0.75F - 1.0E-6F && current <= 1.5F);
            assertTrue(Math.abs(current - previous) < 0.005F,
                    "Adjacent frames must change gradually, without color switching");
            previous = current;
        }
        assertEquals(GroundEffectAppearance.brightness(true, 0.5 - 1.0E-6),
                GroundEffectAppearance.brightness(true, 0.5 + 1.0E-6), 1.0E-5F);
    }

    @Test
    void nonHostileWaterKeepsItsOriginalBrightnessThroughoutTheCycle() {
        for (int frame = 0; frame <= 100; frame++) {
            assertEquals(1.0F, GroundEffectAppearance.brightness(false, frame / 100.0));
        }
    }

    @Test
    void brightColorChannelsSaturateInsteadOfOverflowingTheVertexColorByte() {
        assertEquals(1.0F, GroundEffectAppearance.brightenChannel(232, 1.5F));
        assertEquals(1.0F, GroundEffectAppearance.brightenChannel(255, 1.5F));
        assertEquals(90.0F / 255.0F, GroundEffectAppearance.brightenChannel(60, 1.5F), 1.0E-6F);
        assertEquals(174.0F / 255.0F, GroundEffectAppearance.brightenChannel(232, 0.75F), 1.0E-6F);
    }
}
