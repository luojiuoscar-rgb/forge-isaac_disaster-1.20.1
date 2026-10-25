package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightPhase;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightTimeline;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HolyLightClientStateTest {
    private HolyLightClientState session(long now) {
        HolyLightClientState state = new HolyLightClientState();
        state.useDimension("minecraft:overworld");
        assertTrue(state.synchronize(3, true, now));
        return state;
    }

    @Test
    void repeatedSnapshotsAndSameEpochResetDoNotReplayWarning() {
        var state = session(100);
        var entry = state.add(7, 100, 35, true);
        assertTrue(entry.takeWarningCue());
        assertFalse(entry.takeImpactCue());
        assertTrue(state.synchronize(3, true, 101));
        assertSame(entry, state.add(7, 100, 35, true));
        assertFalse(entry.takeWarningCue());
    }

    @Test
    void anUnconsumedWarningCueCannotPlayAfterFiringBegins() {
        var state = session(100);
        var entry = state.add(7, 100, 35, true);
        state.synchronize(3, false, 135);
        assertEquals(HolyLightPhase.FIRING, entry.phase());
        assertFalse(entry.takeWarningCue(), "A delayed consumer cannot play a warning during firing");
        assertTrue(entry.takeImpactCue());
        state.synchronize(3, false, 155);
        assertEquals(HolyLightPhase.SCORCH, entry.phase());
        assertFalse(entry.takeWarningCue());
    }

    @Test
    void lateScorchJoinAndLateFiringJoinDoNotReplayImpact() {
        var state = session(160);
        var scorch = state.add(7, 100, 35, true);
        assertEquals(HolyLightPhase.SCORCH, scorch.phase());
        assertEquals(60, scorch.age(state.now()), 0.0001);
        assertFalse(scorch.takeWarningCue());
        assertFalse(scorch.takeImpactCue());
        var lateFiring = state.add(8, 123, 35, true);
        assertEquals(HolyLightPhase.FIRING, lateFiring.phase());
        assertFalse(lateFiring.takeImpactCue());
    }

    @Test
    void freshFiringGetsOneImpactWithoutWarningIncludingSkippedWarning() {
        var state = session(135);
        var entry = state.add(7, 100, 35, true);
        assertEquals(HolyLightPhase.FIRING, entry.phase());
        assertTrue(entry.takeImpactCue());
        assertFalse(entry.takeImpactCue());
        var immediate = state.add(8, 135, 0, true);
        assertTrue(immediate.takeImpactCue());
        assertFalse(immediate.takeWarningCue());
    }

    @Test
    void immediateSnapshotOneTickAfterSpawnStillGetsItsSingleImpactCue() {
        var state = session(136);
        var immediate = state.add(8, 135, 0, true);
        assertEquals(HolyLightPhase.FIRING, immediate.phase());
        assertTrue(immediate.takeImpactCue(), "The first end-of-tick snapshot must not lose its impact");
        assertFalse(immediate.takeImpactCue());
        assertFalse(immediate.takeWarningCue());
        state.remove(8);
        assertFalse(state.add(8, 135, 0, true).takeImpactCue());
    }

    @Test
    void warningToFiringRecordsTransitionAndDoesNotRepeatAfterVisibilityReturn() {
        var state = session(134);
        var entry = state.add(7, 100, 35, true);
        assertTrue(entry.takeWarningCue());
        state.tick(false);
        assertEquals(HolyLightPhase.WARNING, entry.previousPhase());
        assertEquals(HolyLightPhase.FIRING, entry.phase());
        assertTrue(entry.takeImpactCue());
        state.remove(7);
        entry = state.add(7, 100, 35, true);
        assertFalse(entry.takeWarningCue());
        assertFalse(entry.takeImpactCue());
    }

    @Test
    void returningDuringWarningCannotReplayWarningButCanSeeFreshImpact() {
        var state = session(100);
        assertTrue(state.add(7, 100, 35, true).takeWarningCue());
        state.remove(7);
        assertTrue(state.synchronize(3, false, 135));
        var entry = state.add(7, 100, 35, true);
        assertTrue(entry.takeImpactCue());
        assertFalse(entry.takeWarningCue());
    }

    @Test
    void clockNeverRewindsAndPausedTicksCannotExpireTheEffect() {
        var state = session(100);
        var entry = state.add(7, 100, 35, true);
        state.tick(false);
        state.synchronize(3, false, 90);
        for (int i = 0; i < 200; i++) state.tick(true);
        assertEquals(101, state.now());
        assertEquals(1, entry.age(state.now()));
        assertSame(entry, state.get(7));
    }

    @Test
    void fractionalRenderTimeFreezesWithoutRewindingWhenPaused() {
        var state = session(100);
        assertEquals(100.75, state.renderTime(0.75F, false));
        assertEquals(100.75, state.renderTime(0, true));
        state.tick(true);
        assertEquals(100.75, state.renderTime(0.1F, true));
        state.tick(false);
        assertEquals(101.25, state.renderTime(0.25F, false));
    }

    @Test
    void expiredEffectsCannotBeRecreatedByOldSnapshots() {
        var state = session(100);
        state.add(7, 100, 35, true);
        state.synchronize(3, false, 255);
        assertNull(state.get(7));
        assertNull(state.add(7, 100, 35, true));
        assertNull(state.add(7, 255, 0, true), "Identity keeps its original start time");
    }

    @Test
    void newEpochClearsEffectsAndRejectsOldEpochWhileDimensionChangeNeedsReset() {
        var state = session(100);
        state.add(7, 100, 35, true);
        assertTrue(state.synchronize(4, true, 10));
        assertNull(state.get(7));
        assertFalse(state.synchronize(3, true, 500));
        assertEquals(10, state.now());
        state.add(8, 10, 0, true);
        state.useDimension("minecraft:the_nether");
        assertTrue(state.entries().isEmpty());
        assertFalse(state.synchronize(4, false, 100));
        assertTrue(state.synchronize(5, true, 100));
        assertTrue(state.add(7, 100, 35, true).takeWarningCue());
    }

    @Test
    void logoutClearAllowsFreshSessionEvenWhenEpochCounterRestarts() {
        var state = session(100);
        state.add(7, 100, 35, true);
        state.clear();
        state.useDimension("minecraft:overworld");
        assertTrue(state.synchronize(1, true, 0));
        assertTrue(state.add(7, 0, 35, true).takeWarningCue());
    }

    @Test
    void worldUnloadRejectsAnOldResetEvenWhenReturningToTheSameDimension() {
        var state = session(100);
        state.add(7, 100, 35, true);
        state.clearForWorldUnload();
        state.useDimension("minecraft:overworld");
        assertFalse(state.synchronize(3, true, 100));
        assertTrue(state.synchronize(4, true, 101));
        assertTrue(state.entries().isEmpty());
    }

    @Test
    void replayHistoryIsBoundedToTheMaximumLifetimeEvenAfterVisibilityExits() throws ReflectiveOperationException {
        var state = session(100);
        for (int i = 1; i <= 1000; i++) {
            state.synchronize(3, false, 99 + i);
            state.add(i, 99 + i, 0, true);
            state.remove(i);
        }
        var historyField = HolyLightClientState.class.getDeclaredField("seen");
        historyField.setAccessible(true);
        assertEquals(HolyLightTimeline.MAX_DURATION, ((Map<?, ?>) historyField.get(state)).size());
        assertNull(state.add(1, 1099, 0, true), "Retired identities cannot return with an altered birth time");
        assertNotNull(state.add(1001, 1099, 0, true));
    }

    @Test
    void newerSkippedExpiryCannotRetireAnUnseenOlderNormalEffect() {
        var state = session(101);
        state.add(2, 101, 0, true);
        state.synchronize(3, false, 221);
        assertNull(state.get(2));
        var older = state.add(1, 100, 35, true);
        assertNotNull(older);
        assertEquals(HolyLightPhase.SCORCH, older.phase());
        assertFalse(older.takeImpactCue());
        state.remove(1);
        assertNotNull(state.add(1, 100, 35, true));
        assertNull(state.add(2, 221, 35, true));
        state.synchronize(3, false, 256);
        assertNull(state.add(1, 256, 35, true));
        assertNull(state.add(2, 256, 35, true));
    }

    @Test
    void retirementWatermarkSurvivesSameEpochResetAndClearsForANewSession() {
        var state = session(100);
        state.add(7, 100, 35, true);
        state.synchronize(3, false, 100 + HolyLightTimeline.MAX_DURATION);
        state.synchronize(3, true, 101 + HolyLightTimeline.MAX_DURATION);
        assertNull(state.add(7, 101 + HolyLightTimeline.MAX_DURATION, 0, true));
        assertTrue(state.synchronize(4, true, 10));
        assertNotNull(state.add(7, 10, 0, true));
        state.synchronize(4, false, 155);
        state.clearForWorldUnload();
        state.useDimension("minecraft:overworld");
        assertTrue(state.synchronize(5, true, 156));
        assertNotNull(state.add(7, 156, 0, true));
    }

    @Test
    void aNewerFastStrikeCannotRetireAnOlderSlowWarning() {
        var state = session(101);
        state.add(2, 101, 0, false);
        state.synchronize(3, false, 300);
        assertNull(state.get(2));
        var slow = state.add(1, 100, 350, true);
        assertNotNull(slow);
        assertEquals(HolyLightPhase.WARNING, slow.phase());
        state.synchronize(3, false, 450);
        assertEquals(HolyLightPhase.FIRING, slow.phase());
        assertTrue(slow.takeImpactCue());
        state.synchronize(3, false, 470);
        assertEquals(HolyLightPhase.SCORCH, slow.phase());
        state.synchronize(3, false, 570);
        assertNull(state.get(1));
    }

    @Test
    void airborneVisualHasNoScorchLifetime() {
        var state = session(100);
        var air = state.add(1, 100, 0, false);
        assertTrue(air.takeImpactCue());
        state.synchronize(3, false, 114);
        assertEquals(HolyLightPhase.FIRING, air.phase());
        state.synchronize(3, false, 115);
        assertNull(state.get(1));
        assertNull(state.add(1, 100, 0, false));
    }
}
