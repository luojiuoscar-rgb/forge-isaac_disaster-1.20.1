package net.luojiuoscar.isaac_disaster.client.gui;

import net.luojiuoscar.isaac_disaster.client.gui.charge_bar.ChargeRingAnimation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChargeRingAnimationTest {
    private static final int GREEN = 0xFF7DE000;

    @Test
    void fullChargeRepeatsWhiteAndRegisteredColorWhileKeepingAlpha() {
        for (long cycle : new long[] { 0, 400, 40_000, 4_000_000 }) {
            assertEquals(0xFFFFFFFF, ChargeRingAnimation.fillColor(1f, GREEN, cycle));
            assertEquals(0xFFFFFFFF, ChargeRingAnimation.fillColor(1f, GREEN, cycle + 199));
            assertEquals(GREEN, ChargeRingAnimation.fillColor(1f, GREEN, cycle + 200));
            assertEquals(GREEN, ChargeRingAnimation.fillColor(1f, GREEN, cycle + 399));
        }
        assertEquals(0x80FFFFFF, ChargeRingAnimation.fillColor(1f, 0x807DE000, 0));
        assertEquals(0xFFFFFFFF, ChargeRingAnimation.fillColor(2f, GREEN, 0));
    }

    @Test
    void partialChargeAndInvalidProgressNeverFlash() {
        for (float progress : new float[] { 0f, 0.5f, 0.999f, -1f, Float.NaN,
                Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY }) {
            for (long time : new long[] { 0, 199, 200, 400 }) {
                assertEquals(GREEN, ChargeRingAnimation.fillColor(progress, GREEN, time));
            }
        }
    }

    @Test
    void loweringProgressImmediatelyRestoresRegisteredColor() {
        assertEquals(0xFFFFFFFF, ChargeRingAnimation.fillColor(1f, GREEN, 100));
        assertEquals(GREEN, ChargeRingAnimation.fillColor(0.75f, GREEN, 100));
    }
}
