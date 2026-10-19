package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickState;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.*;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.LaserBeamBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacketBatchingTest {
    @Test
    void stickyStateBurstPreservesEpochTickAndEveryIdentity() {
        var snapshot = new BulletStickState.Snapshot(
                BulletStickState.Phase.STUCK,
                100, 5, 0, 42, new UUID(1, 2), new Vec3(0, 1, 0));
        var entries = IntStream.range(0, 20_000).mapToObj(slot -> new BulletStickBatchS2CPacket.Entry(slot, 7, new Vec3(slot, 1, 0), Vec3.ZERO, Vec3.ZERO, 0, 0, snapshot, new TrajectoryRuntime(new Vec3(slot, 1, 0), Vec3.ZERO, List.of()).snapshot())).toList();
        var parts = split(new BulletStickBatchS2CPacket(9, 200, entries), BulletStickBatchS2CPacket.class);
        assertTrue(parts.size() > 1);
        List<Integer> slots = new ArrayList<>();
        for (var part : parts) {
            var decoded = roundTrip(part, BulletStickBatchS2CPacket::toBytes, BulletStickBatchS2CPacket::new);
            assertEquals(9, decoded.epoch());
            assertEquals(200, decoded.serverTick());
            decoded.entries().forEach(entry -> {
                assertEquals(7, entry.generation());
                assertEquals(snapshot, entry.stickSnapshot());
                slots.add(entry.slot());
            });
        }
        assertEquals(IntStream.range(0, 20_000).boxed().toList(), slots);
    }

    @Test
    void visibilityExitBurstPreservesReentryReasonAndServerTimeAfterSplitting() {
        var ids = LongStream.range(0, 100_000).boxed().toList();
        var parts = split(new BulletDespawnBatchS2CPacket(9, 123, true, ids), BulletDespawnBatchS2CPacket.class);
        assertTrue(parts.size() > 1);
        List<Long> restored = new ArrayList<>();
        for (var part : parts) {
            var decoded = roundTrip(part, BulletDespawnBatchS2CPacket::toBytes, BulletDespawnBatchS2CPacket::new);
            assertEquals(9, decoded.epoch());
            assertEquals(123, decoded.serverTick());
            assertTrue(decoded.visibilityExit());
            restored.addAll(decoded.identities());
        }
        assertEquals(ids, restored);
    }

    @Test
    void correctionBurstStaysWithinVanillaLimitWithoutLosingSnapshots() {
        var snapshot = BulletState.builder().velocity(new Vec3(1, 0, 0)).build().getTrajectoryRuntime().snapshot();
        var entries = IntStream.range(0, 20_000).mapToObj(slot -> new BulletCorrectionS2CPacket(
                7, slot, 1, new Vec3(slot, 0, 0), new Vec3(1, 0, 0), 0.65F, 4, 4.0, snapshot)).toList();
        var parts = split(new BulletCorrectionBatchS2CPacket(7, entries), BulletCorrectionBatchS2CPacket.class);
        assertTrue(parts.size() > 1, "A large correction burst must be split before sending");
        List<Integer> decodedSlots = new ArrayList<>();
        for (var part : parts) {
            var decoded = roundTrip(part, BulletCorrectionBatchS2CPacket::toBytes, BulletCorrectionBatchS2CPacket::new);
            assertEquals(7, decoded.epoch());
            for (var entry : decoded.entries()) {
                assertEquals(4, entry.age());
                assertNotNull(entry.trajectorySnapshot());
                decodedSlots.add(entry.slot());
            }
        }
        assertEquals(IntStream.range(0, 20_000).boxed().toList(), decodedSlots);
    }

    @Test
    void spawnBurstPreservesAllVariableLengthVisuals() {
        var visual = ResourceLocation.parse("test:" + "v".repeat(600));
        var entries = IntStream.range(0, 2_000).mapToObj(slot -> new BulletSpawnS2CPacket(
                3, slot, 1, 10, Vec3.ZERO, Vec3.ZERO, new Vec3(1, 0, 0), Vec3.ZERO,
                1, 0, 200, 0, 64, 1, 1, 0.1, 0.1, 0xFFFFFF, 1,
                List.of(visual), ResourceLocation.parse("test:tear"), ResourceLocation.parse("test:tear"),
                null, false, false, 0, 0, 0, 0, BulletSteeringMode.LIMITED,
                new TrajectoryRuntime(Vec3.ZERO, new Vec3(1, 0, 0), List.of()).snapshot(),
                false, BulletStickState.Snapshot.flight())).toList();
        var parts = split(new BulletSpawnBatchS2CPacket(entries), BulletSpawnBatchS2CPacket.class);
        assertTrue(parts.size() > 1);
        List<Integer> decodedSlots = new ArrayList<>();
        for (var part : parts) {
            var decoded = roundTrip(part, BulletSpawnBatchS2CPacket::toBytes, BulletSpawnBatchS2CPacket::new);
            for (var entry : decoded.entries()) {
                assertEquals(List.of(visual), entry.visualIds());
                decodedSlots.add(entry.slot());
            }
        }
        assertEquals(IntStream.range(0, 2_000).boxed().toList(), decodedSlots);
    }

    @Test
    void trackingBurstInstallsAssignmentsBeforeTheirTargetSamples() {
        int count = 20_000;
        var targets = IntStream.range(1, count + 1)
                .mapToObj(i -> new BulletTrackingBatchS2CPacket.TargetSample(i, new Vec3(i, 0, 0))).toList();
        var assignments = IntStream.range(0, count)
                .mapToObj(i -> new BulletTrackingBatchS2CPacket.Assignment(i, 1, i + 1, false)).toList();
        var velocities = IntStream.range(0, count).mapToObj(i -> new BulletTrackingBatchS2CPacket.VelocitySample(
                i, 1, new Vec3(1, 0, 0), Vec3.ZERO)).toList();
        var parts = split(new BulletTrackingBatchS2CPacket(9, 40, targets, assignments, velocities),
                BulletTrackingBatchS2CPacket.class);
        assertTrue(parts.size() > 1);
        List<Integer> installedHandles = new ArrayList<>();
        List<Integer> decodedTargets = new ArrayList<>();
        List<Integer> decodedVelocities = new ArrayList<>();
        for (var part : parts) {
            var decoded = roundTrip(part, BulletTrackingBatchS2CPacket::toBytes, BulletTrackingBatchS2CPacket::new);
            assertEquals(9, decoded.epoch());
            assertEquals(40, decoded.sampleTick());
            decoded.assignments().forEach(entry -> installedHandles.add(entry.handle()));
            for (var target : decoded.targets()) {
                assertTrue(installedHandles.contains(target.handle()), "The client ignores unreferenced target samples");
                decodedTargets.add(target.handle());
            }
            decoded.velocitySamples().forEach(entry -> decodedVelocities.add(entry.slot()));
        }
        assertEquals(IntStream.range(1, count + 1).boxed().toList(), installedHandles);
        assertEquals(installedHandles, decodedTargets);
        assertEquals(IntStream.range(0, count).boxed().toList(), decodedVelocities);
    }

    @Test
    void despawnShatterAndLaserBurstsStayWithinVanillaLimit() {
        var identities = IntStream.range(0, 140_000).mapToObj(i -> (long) i).toList();
        var despawns = split(new BulletDespawnBatchS2CPacket(2, 20, false, identities), BulletDespawnBatchS2CPacket.class);
        List<Long> decodedIdentities = new ArrayList<>();
        for (var part : despawns) decodedIdentities.addAll(roundTrip(
                part, BulletDespawnBatchS2CPacket::toBytes, BulletDespawnBatchS2CPacket::new).identities());
        assertEquals(identities, decodedIdentities);

        var shatter = new BulletShatterS2CPacket.Entry(Vec3.ZERO, new Vec3(1, 0, 0), 1, 0xFFFFFF, 1, List.of(), false);
        var shatters = split(new BulletShatterS2CPacket(2, Collections.nCopies(20_000, shatter)),
                BulletShatterS2CPacket.class);
        int decodedShatters = 0;
        for (var part : shatters) decodedShatters += roundTrip(
                part, BulletShatterS2CPacket::toBytes, BulletShatterS2CPacket::new).entries().size();
        assertEquals(20_000, decodedShatters);

        var beam = new LaserBeamBatchS2CPacket.Beam(Vec3.ZERO, new Vec3(1, 0, 0), 1, 0xFFFFFF);
        var lasers = split(new LaserBeamBatchS2CPacket(Collections.nCopies(20_000, beam)),
                LaserBeamBatchS2CPacket.class);
        int decodedBeams = 0;
        for (var part : lasers) decodedBeams += roundTrip(
                part, LaserBeamBatchS2CPacket::toBytes, LaserBeamBatchS2CPacket::new).entries().size();
        assertEquals(20_000, decodedBeams);
    }

    @Test
    void smallTrackingUpdateRemainsOnePacket() {
        var packet = new BulletTrackingBatchS2CPacket(2, 4,
                List.of(new BulletTrackingBatchS2CPacket.TargetSample(1, Vec3.ZERO)),
                List.of(new BulletTrackingBatchS2CPacket.Assignment(0, 1, 1, false)),
                List.of(new BulletTrackingBatchS2CPacket.VelocitySample(0, 1, new Vec3(1, 0, 0), Vec3.ZERO)));
        assertEquals(List.of(packet), split(packet, BulletTrackingBatchS2CPacket.class));
    }

    @Test
    void emptyBatchStillDeliversItsStreamEpoch() {
        var parts = split(new BulletDespawnBatchS2CPacket(12, 20, false, List.of()), BulletDespawnBatchS2CPacket.class);
        assertEquals(1, parts.size());
        var decoded = roundTrip(parts.get(0), BulletDespawnBatchS2CPacket::toBytes, BulletDespawnBatchS2CPacket::new);
        assertEquals(12, decoded.epoch());
        assertTrue(decoded.identities().isEmpty());
    }

    @Test
    void oneOversizedEntryCannotCrashOrDiscardOtherShatterEntries() {
        var hugeVisual = ResourceLocation.parse("test:" + "v".repeat(32_000));
        var huge = new BulletShatterS2CPacket.Entry(Vec3.ZERO, Vec3.ZERO, 1, 0, 1,
                Collections.nCopies(34, hugeVisual), false);
        var normal = new BulletShatterS2CPacket.Entry(new Vec3(2, 0, 0), Vec3.ZERO, 1, 0, 1, List.of(), false);
        var parts = split(new BulletShatterS2CPacket(1, List.of(normal, huge, normal)), BulletShatterS2CPacket.class);
        int decodedEntries = 0;
        for (var part : parts) {
            var decoded = roundTrip(part, BulletShatterS2CPacket::toBytes, BulletShatterS2CPacket::new);
            decodedEntries += decoded.entries().size();
            decoded.entries().forEach(entry -> assertEquals(new Vec3(2, 0, 0), entry.position()));
        }
        assertEquals(2, decodedEntries);
    }

    private static <P> List<P> split(P packet, Class<P> type) {
        List<P> result = new ArrayList<>();
        PacketBatching.forEach(packet, part -> result.add(type.cast(part)));
        return result;
    }

    private static <P> P roundTrip(P packet, BiConsumer<P, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, P> decoder) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            encoder.accept(packet, buffer);
            assertTrue(buffer.readableBytes() + 5 <= 1_048_576, "Include room for the Forge message discriminator");
            assertDoesNotThrow(() -> new ClientboundCustomPayloadPacket(ResourceLocation.parse("test:bullets"), buffer));
            P decoded = decoder.apply(buffer);
            assertEquals(0, buffer.readableBytes());
            return decoded;
        } finally {
            buffer.release();
        }
    }
}
