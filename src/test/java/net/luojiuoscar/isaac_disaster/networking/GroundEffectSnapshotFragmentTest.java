package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import net.luojiuoscar.isaac_disaster.groundeffect.client.GroundEffectSnapshotAssembler;
import net.luojiuoscar.isaac_disaster.groundeffect.client.GroundEffectStream;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectCoverage;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotFragmentS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroundEffectSnapshotFragmentTest {
    @Test
    void normalSnapshotsKeepTheirOrderAroundAFragmentedSnapshot() {
        var before = snapshot(1, GroundEffectCoverage.puddle(0.5D, 1));
        var complex = snapshot(2, coverage(12_000));
        var after = snapshot(3, GroundEffectCoverage.puddle(0.5D, 3));
        var parts = GroundEffectPacketBatchingTest.split(new GroundEffectSnapshotBatchS2CPacket(8, 123, false,
                List.of(before, complex, after)));
        List<GroundEffectSnapshotBatchS2CPacket.Entry> restored = new ArrayList<>();
        var assembler = new GroundEffectSnapshotAssembler();
        for (Object part : parts) {
            if (part instanceof GroundEffectSnapshotBatchS2CPacket batch) restored.addAll(batch.entries());
            else assembler.accept((GroundEffectSnapshotFragmentS2CPacket) part).ifPresent(result -> restored.add(result.entry()));
        }
        assertEquals(List.of(before, complex, after), restored);
    }

    @Test
    void oversizedSpawnAndUpdateBecomeVisibleOnlyAfterCompleteLosslessAssembly() {
        var complex = snapshot(3, coverage(40_000));
        for (boolean update : List.of(false, true)) {
            Object packet = update
                    ? new GroundEffectSnapshotBatchS2CPacket(8, 123, true, List.of(complex))
                    : new GroundEffectSnapshotBatchS2CPacket(8, 123, false, List.of(complex));
            var parts = GroundEffectPacketBatchingTest.split(packet);
            assertTrue(parts.size() > 2);
            var assembler = new GroundEffectSnapshotAssembler();
            var stream = new GroundEffectStream();
            var old = snapshot(2, GroundEffectCoverage.puddle(0.5D, 1));
            if (update) stream.spawn(old);
            for (int i = 0; i < parts.size(); i++) {
                var fragment = GroundEffectPacketBatchingTest.roundTrip(
                        (GroundEffectSnapshotFragmentS2CPacket) parts.get(i),
                        GroundEffectSnapshotFragmentS2CPacket::toBytes, GroundEffectSnapshotFragmentS2CPacket::new);
                assertEquals(8, fragment.epoch());
                Optional<GroundEffectSnapshotAssembler.Result> result = assembler.accept(fragment);
                if (i < parts.size() - 1) {
                    assertTrue(result.isEmpty());
                    if (update) assertEquals(old.coverage(), stream.get(1, 2).coverage());
                    else assertEquals(0, stream.size());
                } else {
                    var completed = result.orElseThrow();
                    assertEquals(update, completed.update());
                    assertEquals(123, completed.serverTick());
                    assertEquals(complex, completed.entry());
                    assertEquals(complex.coverage().pieces(), completed.entry().coverage().pieces());
                    if (update) assertTrue(stream.applyUpdate(completed.entry()));
                    else stream.spawn(completed.entry());
                    assertEquals(complex.coverage(), stream.get(1, 2).coverage());
                }
            }
        }
    }

    @Test
    void gapsOverlapsAndMetadataChangesRejectTheWholeTransfer() {
        byte[] encoded = encode(snapshot(3, coverage(100)));
        for (int wrongOffset : List.of(0, 63, 65)) {
            var assembler = new GroundEffectSnapshotAssembler();
            assertTrue(assembler.accept(fragment(1, encoded, 0, 64)).isEmpty());
            assertTrue(assembler.accept(fragment(1, encoded, wrongOffset, 64)).isEmpty());
            assertTrue(assembler.accept(fragment(1, encoded, 64, encoded.length - 64)).isEmpty());
        }
        var assembler = new GroundEffectSnapshotAssembler();
        assembler.accept(fragment(2, encoded, 0, 64));
        assertTrue(assembler.accept(new GroundEffectSnapshotFragmentS2CPacket(8, 124, false,
                2, encoded.length, 64, Arrays.copyOfRange(encoded, 64, encoded.length))).isEmpty());
        assertTrue(assembler.accept(fragment(2, encoded, 64, encoded.length - 64)).isEmpty());
    }

    @Test
    void newerTransferSupersedesIncompleteOldDataAndRejectsOldReplay() {
        byte[] encoded = encode(snapshot(3, coverage(100)));
        var assembler = new GroundEffectSnapshotAssembler();
        assembler.accept(fragment(1, encoded, 0, 64));
        assembler.accept(fragment(2, encoded, 0, 64));
        assertTrue(assembler.accept(fragment(1, encoded, 64, encoded.length - 64)).isEmpty());
        assertTrue(assembler.accept(fragment(2, encoded, 64, encoded.length - 64)).isPresent());
        assertTrue(assembler.accept(fragment(1, encoded, 0, encoded.length)).isEmpty());
        assertTrue(assembler.accept(fragment(2, encoded, 0, encoded.length)).isEmpty());
    }

    @Test
    void timeoutAndWorldClearReleaseIncompleteTransfer() {
        byte[] encoded = encode(snapshot(3, coverage(100)));
        AtomicLong now = new AtomicLong();
        var assembler = new GroundEffectSnapshotAssembler(now::get);
        assembler.accept(fragment(1, encoded, 0, 64));
        now.set(31_000_000_000L);
        assembler.expireInactive();
        assertTrue(assembler.accept(fragment(1, encoded, 64, encoded.length - 64)).isEmpty());
        assembler.clear();
        assertTrue(assembler.accept(fragment(1, encoded, 0, encoded.length)).isPresent());
    }

    @Test
    void illegalLengthsMalformedPayloadAndTrailingBytesNeverProduceSnapshots() {
        byte[] encoded = encode(snapshot(3, coverage(10)));
        var assembler = new GroundEffectSnapshotAssembler();
        assertTrue(assembler.accept(new GroundEffectSnapshotFragmentS2CPacket(8, 123, false,
                1, 32 * 1024 * 1024 + 1, 0, new byte[]{1})).isEmpty());
        assertTrue(assembler.accept(new GroundEffectSnapshotFragmentS2CPacket(8, 123, false,
                2, encoded.length, -1, new byte[]{1})).isEmpty());
        assertTrue(assembler.accept(new GroundEffectSnapshotFragmentS2CPacket(8, 123, false,
                3, 1, 0, new byte[]{1})).isEmpty());
        byte[] trailing = Arrays.copyOf(encoded, encoded.length + 1);
        assertTrue(assembler.accept(fragment(4, trailing, 0, trailing.length)).isEmpty());
        assertTrue(assembler.accept(fragment(5, encoded, 0, encoded.length)).isPresent());
    }

    @Test
    void impossibleSnapshotIsRejectedBeforeAnyPartOfItsBatchIsSent() {
        var oversized = snapshot(3, coverage(700_000));
        List<Object> sent = new ArrayList<>();
        var batch = new GroundEffectSnapshotBatchS2CPacket(8, 123, false,
                List.of(snapshot(1, GroundEffectCoverage.puddle(0.5D, 1)), oversized));
        assertThrows(IllegalArgumentException.class, () -> PacketBatching.forEach(batch, sent::add));
        assertTrue(sent.isEmpty());
    }

    private static GroundEffectSnapshotFragmentS2CPacket fragment(long id, byte[] all, int offset, int length) {
        return new GroundEffectSnapshotFragmentS2CPacket(8, 123, false, id, all.length, offset,
                Arrays.copyOfRange(all, offset, offset + length));
    }

    private static GroundEffectSnapshotBatchS2CPacket.Entry snapshot(int revision, GroundEffectCoverage coverage) {
        return new GroundEffectSnapshotBatchS2CPacket.Entry(1, 2, Vec3.ZERO, 0.5D, 7, 120, 1, null,
                ResourceLocation.parse("isaac_disaster:water"), 0x3C9BE8, 1, revision, coverage);
    }

    private static byte[] encode(GroundEffectSnapshotBatchS2CPacket.Entry entry) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            entry.toBytes(buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            return data;
        } finally {
            buf.release();
        }
    }

    /** Protocol fixture with many intact triangles; no production clipping is altered. */
    private static GroundEffectCoverage coverage(int triangles) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeBoolean(false);
            buf.writeVarInt(triangles);
            for (int i = 0; i < triangles; i++) {
                buf.writeVarInt(3);
                buf.writeDouble(i).writeDouble(0);
                buf.writeDouble(i + 0.5D).writeDouble(0);
                buf.writeDouble(i).writeDouble(0.5D);
            }
            return GroundEffectCoverage.read(buf, 0.5D, 1);
        } finally {
            buf.release();
        }
    }
}
