package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client.GroundEffectStream;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.GroundEffectTestFixtures.*;

import java.util.UUID;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroundEffectStreamTest {
    private static GroundEffectSnapshotBatchS2CPacket.Entry entry(int slot, int generation, int age, int lifetime) {
        return new GroundEffectSnapshotBatchS2CPacket.Entry(slot, generation, new Vec3(1, 2, 3),
                0.5, age, lifetime, generation, new UUID(1, 2),
                ResourceLocation.fromNamespaceAndPath("isaac_disaster", "water"), 0x3C9BE8, generation);
    }

    @Test
    void localAnimationKeepsTwentyTicksPerSecondWhenServerClockSlowsOrMovesAhead() {
        AtomicLong nanos = new AtomicLong();
        var stream = new GroundEffectStream(nanos::get);
        var state = stream.spawn(animatedEntry(0, 100, 0), 100);
        nanos.set(1_000_000_000L);
        assertEquals(0.2, state.animationScale(stream.localAnimationTick()), 1e-9);
        stream.synchronize(105);
        assertEquals(0.2, state.animationScale(stream.localAnimationTick()), 1e-9);
        nanos.set(2_000_000_000L);
        assertEquals(0.4, state.animationScale(stream.localAnimationTick()), 1e-9);
        stream.synchronize(500);
        assertEquals(0.4, state.animationScale(stream.localAnimationTick()), 1e-9);
        stream.tick();
        assertEquals(99, state.age(), "Lifecycle prediction still follows the server clock");
    }

    @Test
    void snapshotUpdatesPreserveTheLocalAnimationOriginAndCachedTriangles() {
        AtomicLong nanos = new AtomicLong();
        var stream = new GroundEffectStream(nanos::get);
        var state = stream.spawn(animatedEntry(0, 100, 10), 100);
        var triangles = state.triangleVertices();
        nanos.set(1_000_000_000L);
        assertEquals(0.3, state.animationScale(stream.localAnimationTick()), 1e-9);
        assertTrue(stream.applyUpdate(animatedEntry(1, 100, 0), 105));
        state = stream.get(1, 1);
        assertEquals(0.3, state.animationScale(stream.localAnimationTick()), 1e-9);
        assertSame(triangles, state.triangleVertices());
        assertTrue(stream.applyUpdate(animatedEntry(2, 0, 100), 500));
        state = stream.get(1, 1);
        assertEquals(0.3, state.animationScale(stream.localAnimationTick()), 1e-9,
                "A server completion update cannot finish or restart local growth");
        nanos.set(2_000_000_000L);
        assertEquals(0.5, state.animationScale(stream.localAnimationTick()), 1e-9);
        assertSame(triangles, state.triangleVertices());
    }

    @Test
    void lateVisibilityEntryStartsAtSnapshotElapsedAndAdvancesLocally() {
        AtomicLong nanos = new AtomicLong(10_000_000_000L);
        var stream = new GroundEffectStream(nanos::get);
        var state = stream.spawn(animatedEntry(0, 100, 60), 500);
        assertEquals(0.6, state.animationScale(stream.localAnimationTick()), 1e-9);
        nanos.addAndGet(250_000_000L);
        assertEquals(0.65, state.animationScale(stream.localAnimationTick()), 1e-9);
        assertTrue(stream.visibilityExit(1, 1));
        state = stream.spawn(animatedEntry(1, 100, 80), 510);
        assertEquals(0.8, state.animationScale(stream.localAnimationTick()), 1e-9);
    }

    @Test
    void localAnimationClockNeverMovesBackward() {
        AtomicLong nanos = new AtomicLong();
        var stream = new GroundEffectStream(nanos::get);
        var state = stream.spawn(animatedEntry(0, 100, 0), 100);
        nanos.set(1_000_000_000L);
        assertEquals(0.2, state.animationScale(stream.localAnimationTick()), 1e-9);
        nanos.set(500_000_000L);
        assertEquals(0.2, state.animationScale(stream.localAnimationTick()), 1e-9);
        stream.synchronize(90);
        assertEquals(0.2, state.animationScale(stream.localAnimationTick()), 1e-9);
    }

    private static GroundEffectSnapshotBatchS2CPacket.Entry animatedEntry(int revision, int duration, int elapsed) {
        return new GroundEffectSnapshotBatchS2CPacket.Entry(1, 1, Vec3.ZERO, 2, 0, 100,
                revision, null, ResourceLocation.parse("isaac_disaster:water"), 0x3C9BE8, 1,
                revision, duration, elapsed);
    }

    @Test
    void streamRejectsOlderEpochAndClearsWhenEnteringNewWorld() {
        GroundEffectStream stream = new GroundEffectStream();
        assertTrue(stream.beginSession(4));
        var state = stream.spawn(entry(1, 1, 0, 20));

        assertFalse(stream.acceptEpoch(3));
        assertEquals(1, stream.size());
        assertSame(state, stream.get(1, 1));
        assertFalse(stream.acceptEpoch(5), "Data packets cannot implicitly start another world session");
        assertTrue(stream.beginSession(5));
        assertEquals(0, stream.size());
    }

    @Test
    void newerGenerationReplacesTheSlotAndOlderGenerationCannotResurrectIt() {
        GroundEffectStream stream = new GroundEffectStream();
        stream.spawn(entry(2, 4, 0, 20));

        assertNull(stream.spawn(entry(2, 3, 0, 20)));
        var replacement = stream.spawn(entry(2, 5, 0, 20));
        assertEquals(1, stream.size());
        assertEquals(replacement, stream.get(2, 5));
        assertNull(stream.get(2, 4));
    }

    @Test
    void predictedExpiryRetainsAVisibleCopyUntilAuthoritativeDespawn() {
        AtomicLong nanos = new AtomicLong();
        GroundEffectStream stream = new GroundEffectStream(nanos::get);
        var water = stream.spawn(entry(3, 1, 2, 4), 100);

        nanos.set(50_000_000L);
        stream.tick();
        assertEquals(3, stream.get(3, 1).age());
        nanos.set(10_000_000_000L);
        stream.tick();
        assertEquals(1, stream.size());
        assertEquals(3, water.age(), "Prediction must not make a server-owned puddle completely invisible");
        stream.despawn(water.identity());
        assertEquals(0, stream.size());
    }

    @Test
    void visibilityExitAllowsAnOrderedSameGenerationReentry() {
        GroundEffectStream stream = new GroundEffectStream();
        stream.spawn(entry(4, 2, 0, 20));
        assertTrue(stream.visibilityExit(4, 2));
        assertEquals(0, stream.size());
        assertTrue(stream.spawn(entry(4, 2, 3, 20)) != null);
        assertEquals(1, stream.size());
    }

    @Test
    void updatesPreserveTheCompleteFootprintAndRejectOlderRevisionsOrGenerations() {
        var stream = new GroundEffectStream();
        var before = animatedEntry(0, 100, 0);
        var state = stream.spawn(before);
        assertTrue(state.coverage().contains(0, 0));
        assertTrue(stream.applyUpdate(animatedEntry(1, 100, 20)));
        assertTrue(stream.get(1, 1).coverage().contains(0, 0));
        assertFalse(stream.applyUpdate(before));
        stream.despawn(state.identity());
        assertFalse(stream.applyUpdate(animatedEntry(2, 100, 40)));
        assertEquals(0, stream.size());
    }

    @Test
    void partialOverlapKeepsBothCompleteClientOutlines() {
        var manager = new GroundEffectManager();
        var old = water(manager, new UUID(1, 1), Vec3.ZERO, 1, 100, 1);
        var stream = new GroundEffectStream();
        var oldState = stream.spawn(snapshot(old));
        var triangles = oldState.triangleVertices();
        var newer = water(manager, new UUID(2, 2), Vec3.ZERO, 0.25, 20, 1);
        var newerState = stream.spawn(snapshot(newer));
        assertEquals(2, stream.size());
        assertTrue(oldState.coverage().contains(0, 0));
        assertTrue(newerState.coverage().contains(0, 0));
        assertTrue(manager.drainDespawns().isEmpty());
        stream.despawn(newer.identity());
        assertTrue(oldState.coverage().contains(0, 0));
        assertSame(triangles, oldState.triangleVertices());
    }

    @Test
    void completeCoverageRemovesTheOldClientStateThroughDespawn() {
        var manager = new GroundEffectManager();
        var old = water(manager, new UUID(1, 1), Vec3.ZERO, 0.25, 100, 1);
        var stream = new GroundEffectStream();
        stream.spawn(snapshot(old));
        var newer = water(manager, new UUID(2, 2), Vec3.ZERO, 1, 20, 1);
        stream.spawn(snapshot(newer));
        var removed = manager.drainDespawns();
        assertEquals(Set.of(old.identity()), removed);
        removed.forEach(stream::despawn);
        assertEquals(1, stream.size());
        assertNull(stream.get(old.slot(), old.generation()));
        assertTrue(stream.get(newer.slot(), newer.generation()).coverage().contains(0, 0));
    }

    @Test
    void refreshUpdateResetsPredictedAgeButCannotUndoATerminalDespawn() {
        GroundEffectManager manager = new GroundEffectManager();
        UUID owner = new UUID(1, 1);
        var water = water(manager, owner, Vec3.ZERO, 0.5D, 1, 1.0F);
        GroundEffectStream stream = new GroundEffectStream();
        stream.spawn(snapshot(water));
        stream.tick();
        assertEquals(1, stream.size());
        water(manager, owner, Vec3.ZERO, 0.5D, 1, 1.0F);
        var update = snapshot(water);
        assertTrue(stream.applyUpdate(update));
        assertEquals(0, stream.get(water.slot(), water.generation()).age());
        stream.tick();
        stream.despawn(water.identity());
        assertFalse(stream.applyUpdate(update));
    }

    @Test
    void oldLifecyclePacketsCannotReviveAnUnloadedWorldAndReturnVisitUsesANewSession() {
        var stream = new GroundEffectStream();
        assertTrue(stream.beginSession(1));
        stream.spawn(entry(1, 1, 0, 100), 10);
        stream.clearForWorldUnload();
        assertFalse(stream.acceptEpoch(1));
        assertFalse(stream.beginSession(1));
        assertTrue(stream.beginSession(2));
        stream.clearForWorldUnload();
        assertTrue(stream.beginSession(3));
        assertTrue(stream.acceptEpoch(3));
        assertFalse(stream.acceptEpoch(1));
        assertFalse(stream.acceptEpoch(2));
        assertEquals(0, stream.size());
    }

    @Test
    void serverClockSamplesKeepSlowServerPuddlesAtTheCorrectAge() {
        AtomicLong nanos = new AtomicLong();
        var stream = new GroundEffectStream(nanos::get);
        var water = stream.spawn(entry(1, 1, 0, 100), 100);
        nanos.set(2_000_000_000L);
        stream.synchronize(120);
        nanos.set(5_000_000_000L);
        stream.tick();
        assertEquals(50, water.age(), "Five wall-clock seconds on a 10 TPS server are fifty server ticks");
        assertEquals(1, stream.size());
    }

    @Test
    void renderOrderIsCachedAndInvalidatedWhenARepaintChangesCreationOrder() {
        GroundEffectManager manager = new GroundEffectManager();
        var owner = new UUID(1, 2);
        var first = water(manager, owner, Vec3.ZERO, 0.5, 100, 1.0F);
        var second = water(manager, owner, new Vec3(3, 0, 0), 0.5, 100, 1.0F);
        var stream = new GroundEffectStream();
        stream.spawn(snapshot(second));
        stream.spawn(snapshot(first));
        var cached = stream.states();
        assertEquals(first.identity(), cached.get(0).identity());
        assertSame(cached, stream.states());
        stream.tick();
        assertSame(cached, stream.states(), "A time tick does not change draw ordering");
        water(manager, owner, Vec3.ZERO, 0.5, 100, 1.0F);
        stream.applyUpdate(snapshot(first));
        assertEquals(second.identity(), stream.states().get(0).identity());
        assertEquals(first.identity(), stream.states().get(1).identity());
    }

    @Test
    void completeFootprintTrianglesAreCachedAndRemainInsideThePuddle() {
        var stream = new GroundEffectStream();
        var state = stream.spawn(entry(1, 1, 0, 100));
        var triangles = state.triangleVertices();
        assertTrue(triangles.size() > 0 && triangles.size() % 3 == 0);
        for (int i = 0; i < triangles.size(); i += 3) {
            double x = (triangles.get(i).x() + triangles.get(i + 1).x() + triangles.get(i + 2).x()) / 3;
            double z = (triangles.get(i).z() + triangles.get(i + 1).z() + triangles.get(i + 2).z()) / 3;
            assertTrue(state.coverage().contains(x, z));
        }
        assertSame(triangles, state.triangleVertices());
    }
}
