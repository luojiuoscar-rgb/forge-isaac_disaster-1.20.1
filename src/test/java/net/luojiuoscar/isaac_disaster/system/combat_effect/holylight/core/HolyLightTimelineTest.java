package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HolyLightTimelineTest {
    @Test
    void normalStrikeUsesHalfOpenWarningFiringAndScorchIntervals() {
        assertPhase(0, false, HolyLightPhase.WARNING, 0);
        assertPhase(34.99, false, HolyLightPhase.WARNING, 34.99);
        assertPhase(35, false, HolyLightPhase.FIRING, 0);
        assertPhase(49.99, false, HolyLightPhase.FIRING, 14.99);
        assertPhase(50, false, HolyLightPhase.SCORCH, 0);
        assertPhase(149.99, false, HolyLightPhase.SCORCH, 99.99);
        assertPhase(150, false, HolyLightPhase.EXPIRED, 0);
        assertEquals(150, HolyLightTimeline.duration(false));
    }

    @Test
    void skippedWarningStartsFiringImmediatelyAndExpiresEarlier() {
        assertPhase(0, true, HolyLightPhase.FIRING, 0);
        assertPhase(14.99, true, HolyLightPhase.FIRING, 14.99);
        assertPhase(15, true, HolyLightPhase.SCORCH, 0);
        assertPhase(114.99, true, HolyLightPhase.SCORCH, 99.99);
        assertPhase(115, true, HolyLightPhase.EXPIRED, 0);
        assertEquals(115, HolyLightTimeline.duration(true));
    }

    @Test
    void negativeAgesClampToStartAndInvalidAgesExpire() {
        assertPhase(-20, false, HolyLightPhase.WARNING, 0);
        assertPhase(-20, true, HolyLightPhase.FIRING, 0);
        assertPhase(Double.NaN, false, HolyLightPhase.EXPIRED, 0);
        assertPhase(Double.POSITIVE_INFINITY, false, HolyLightPhase.EXPIRED, 0);
    }

    private static void assertPhase(double age, boolean skipped, HolyLightPhase expected, double phaseAge) {
        assertEquals(expected, HolyLightTimeline.phase(age, skipped));
        assertEquals(phaseAge, HolyLightTimeline.phaseAge(age, skipped), 1.0E-8);
    }

    @Test
    void warningSpeedScalesOnlyTheWarningAndNormalizesUnsafeInputs() {
        assertEquals(35, HolyLightTimeline.warningTicks(1, false));
        assertEquals(18, HolyLightTimeline.warningTicks(2, false));
        assertEquals(70, HolyLightTimeline.warningTicks(.5, false));
        assertEquals(0, HolyLightTimeline.warningTicks(.5, true));
        for (double speed : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(35, HolyLightTimeline.warningTicks(speed, false));
        }
        assertEquals(700, HolyLightTimeline.warningTicks(Double.MIN_VALUE, false));
        assertEquals(1, HolyLightTimeline.warningTicks(Double.MAX_VALUE, false));
        assertEquals(HolyLightPhase.WARNING, HolyLightTimeline.phase(69.99, 70, true));
        assertEquals(HolyLightPhase.FIRING, HolyLightTimeline.phase(70, 70, true));
        assertEquals(0, HolyLightTimeline.phaseAge(70, 70, true));
        assertEquals(HolyLightPhase.FIRING, HolyLightTimeline.phase(84.99, 70, true));
        assertEquals(HolyLightPhase.SCORCH, HolyLightTimeline.phase(85, 70, true));
        assertEquals(185, HolyLightTimeline.duration(70, true));
    }

    @Test
    void airborneStrikesSkipScorchAndExpireAsSoonAsFiringFinishes() {
        assertEquals(HolyLightPhase.FIRING, HolyLightTimeline.phase(14.99, 0, false));
        assertEquals(HolyLightPhase.EXPIRED, HolyLightTimeline.phase(15, 0, false));
        assertEquals(15, HolyLightTimeline.duration(0, false));
        assertEquals(HolyLightPhase.FIRING, HolyLightTimeline.phase(84.99, 70, false));
        assertEquals(HolyLightPhase.EXPIRED, HolyLightTimeline.phase(85, 70, false));
        assertEquals(85, HolyLightTimeline.duration(70, false));
    }
}
