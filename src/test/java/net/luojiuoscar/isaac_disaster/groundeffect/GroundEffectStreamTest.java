package net.luojiuoscar.isaac_disaster.groundeffect;

import net.luojiuoscar.isaac_disaster.groundeffect.client.GroundEffectStream;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;
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
    void clippedSnapshotKeepsItsHoleForALateViewerAfterTheCoveringEffectIsGone() {
        GroundEffectManager manager = new GroundEffectManager();
        var old = manager.spawnWater(new UUID(1, 1), Vec3.ZERO, 1.0D, 100);
        var covering = manager.spawnWater(new UUID(2, 2), Vec3.ZERO, 0.25D, 20);
        manager.remove(covering);
        GroundEffectStream stream = new GroundEffectStream();
        var snapshot = GroundEffectSnapshotBatchS2CPacket.Entry.from(old);
        var state = stream.spawn(snapshot);
        assertFalse(state.coverage().contains(0, 0));
        assertTrue(state.coverage().contains(0.7D, 0));
        stream.visibilityExit(old.slot(), old.generation());
        assertFalse(stream.spawn(snapshot).coverage().contains(0, 0));
    }

    @Test
    void updatesClipLiveStatesAndRejectOlderRevisionsOrGenerations() {
        GroundEffectManager manager = new GroundEffectManager();
        var old = manager.spawnWater(new UUID(1, 1), Vec3.ZERO, 1.0D, 100);
        GroundEffectStream stream = new GroundEffectStream();
        var before = GroundEffectSnapshotBatchS2CPacket.Entry.from(old);
        stream.spawn(before);
        manager.spawnWater(new UUID(2, 2), Vec3.ZERO, 0.25D, 20);
        var changed = GroundEffectSnapshotBatchS2CPacket.Entry.from(old);
        assertTrue(stream.applyUpdate(changed));
        assertFalse(stream.get(old.slot(), old.generation()).coverage().contains(0, 0));
        assertFalse(stream.applyUpdate(before));
        stream.despawn(old.identity());
        assertFalse(stream.applyUpdate(changed));
        assertEquals(0, stream.size());
    }

    @Test
    void refreshUpdateResetsPredictedAgeButCannotUndoATerminalDespawn() {
        GroundEffectManager manager = new GroundEffectManager();
        UUID owner = new UUID(1, 1);
        var water = manager.spawnWater(owner, Vec3.ZERO, 0.5D, 1);
        GroundEffectStream stream = new GroundEffectStream();
        stream.spawn(GroundEffectSnapshotBatchS2CPacket.Entry.from(water));
        stream.tick();
        assertEquals(1, stream.size());
        manager.spawnWater(owner, Vec3.ZERO, 0.5D, 1);
        var update = GroundEffectSnapshotBatchS2CPacket.Entry.from(water);
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
        var first = manager.spawnWater(owner, Vec3.ZERO, 0.5, 100);
        var second = manager.spawnWater(owner, new Vec3(3, 0, 0), 0.5, 100);
        var stream = new GroundEffectStream();
        stream.spawn(GroundEffectSnapshotBatchS2CPacket.Entry.from(second));
        stream.spawn(GroundEffectSnapshotBatchS2CPacket.Entry.from(first));
        var cached = stream.states();
        assertEquals(first.identity(), cached.get(0).identity());
        assertSame(cached, stream.states());
        stream.tick();
        assertSame(cached, stream.states(), "A time tick does not change draw ordering");
        manager.spawnWater(owner, Vec3.ZERO, 0.5, 100);
        stream.applyUpdate(GroundEffectSnapshotBatchS2CPacket.Entry.from(first));
        assertEquals(second.identity(), stream.states().get(0).identity());
        assertEquals(first.identity(), stream.states().get(1).identity());
    }

    @Test
    void cachedTrianglesDoNotFillAClippedHole() {
        GroundEffectManager manager = new GroundEffectManager();
        var old = manager.spawnWater(new UUID(1, 1), Vec3.ZERO, 1.0D, 100);
        var hole = manager.spawnWater(new UUID(2, 2), Vec3.ZERO, 0.25D, 20);
        GroundEffectStream stream = new GroundEffectStream();
        var state = stream.spawn(GroundEffectSnapshotBatchS2CPacket.Entry.from(old));
        var triangles = state.triangleVertices();
        assertTrue(triangles.size() > 0 && triangles.size() % 3 == 0);
        for (int i = 0; i < triangles.size(); i += 3) {
            double x = (triangles.get(i).x() + triangles.get(i + 1).x() + triangles.get(i + 2).x()) / 3;
            double z = (triangles.get(i).z() + triangles.get(i + 1).z() + triangles.get(i + 2).z()) / 3;
            assertTrue(old.coverage().contains(x, z));
            assertFalse(hole.coverage().contains(x, z));
        }
        assertSame(triangles, state.triangleVertices());
    }
}
