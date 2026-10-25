package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HolyLightWarningSoundTest {
    private HolyLightClientState session() {
        var state = new HolyLightClientState();
        state.useDimension("minecraft:overworld");
        state.synchronize(3, true, 100);
        return state;
    }

    private HolyLightSnapshot snapshot(long id, int warningTicks) {
        return new HolyLightSnapshot(id, new Vec3(2, 64, 2), .8, 16, 100, warningTicks, true, id);
    }

    @Test
    void aLongWarningClipStopsAtFiringInsteadOfContinuingIntoScorch() {
        var state = session();
        state.add(7, 100, 35, true);
        var sound = new HolyLightWarningSound(snapshot(7, 35), state);
        assertTrue(sound.canPlaySound());
        assertFalse(sound.isLooping());
        state.synchronize(3, false, 134);
        sound.tick();
        assertFalse(sound.isStopped());
        state.synchronize(3, false, 135);
        assertFalse(sound.canPlaySound());
        sound.tick();
        assertTrue(sound.isStopped());
        state.synchronize(3, false, 155);
        assertFalse(sound.canPlaySound());
    }

    @Test
    void removalStopsOnlyThatStrikesWarningAndWorldClearStopsTheOthers() {
        var state = session();
        state.add(7, 100, 35, true);
        state.add(8, 100, 70, true);
        var first = new HolyLightWarningSound(snapshot(7, 35), state);
        var second = new HolyLightWarningSound(snapshot(8, 70), state);
        state.remove(7);
        first.tick();
        second.tick();
        assertTrue(first.isStopped());
        assertFalse(second.isStopped());
        state.clearForWorldUnload();
        second.tick();
        assertTrue(second.isStopped());
    }

    @Test
    void immediateAndLateFiringSnapshotsCannotStartAWarningSound() {
        var state = session();
        state.add(7, 100, 0, true);
        var immediate = new HolyLightWarningSound(snapshot(7, 0), state);
        assertFalse(immediate.canPlaySound());
        immediate.tick();
        assertTrue(immediate.isStopped());
        state.synchronize(3, false, 136);
        state.add(8, 100, 35, true);
        var late = new HolyLightWarningSound(snapshot(8, 35), state);
        assertFalse(late.canPlaySound());
    }

    @Test
    void explicitCancellationCannotReviveAfterVisibilityReturn() {
        var state = session();
        state.add(7, 100, 35, true);
        var sound = new HolyLightWarningSound(snapshot(7, 35), state);
        sound.cancel();
        state.remove(7);
        state.add(7, 100, 35, true);
        assertTrue(sound.isStopped());
        assertFalse(sound.canPlaySound());
    }
}
