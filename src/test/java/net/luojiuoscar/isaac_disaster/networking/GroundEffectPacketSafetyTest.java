package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GroundEffectPacketSafetyTest {
    @Test
    void truncatedPacketsAreIgnoredWithoutEscapingTheDecoder() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            assertEquals(0, assertDoesNotThrow(() -> new GroundEffectSnapshotBatchS2CPacket(buf)).epoch());
            assertEquals(0, assertDoesNotThrow(() -> new GroundEffectDespawnBatchS2CPacket(buf)).epoch());
            assertEquals(0, assertDoesNotThrow(() -> new GroundEffectSyncS2CPacket(buf)).epoch());
        } finally { buf.release(); }
    }

    @Test
    void maliciousCollectionCountsCannotAllocateOrApplyPartialPackets() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buf.writeVarInt(7);
            buf.writeVarInt(10);
            buf.writeBoolean(false);
            buf.writeVarInt(Integer.MAX_VALUE);
            assertEquals(0, assertDoesNotThrow(() -> new GroundEffectSnapshotBatchS2CPacket(buf)).epoch());
            buf.clear();
            buf.writeVarInt(7);
            buf.writeVarInt(10);
            buf.writeBoolean(false);
            buf.writeVarInt(2);
            buf.writeLong(100);
            var discarded = assertDoesNotThrow(() -> new GroundEffectDespawnBatchS2CPacket(buf));
            assertEquals(0, discarded.epoch());
            assertTrue(discarded.identities().isEmpty());
        } finally { buf.release(); }
    }

    @Test
    void truncatedSecondSnapshotDiscardsTheWholeBatch() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new GroundEffectSnapshotBatchS2CPacket(8, 100, false,
                    List.of(entry(1, 0.5), entry(2, 1))).toBytes(buf);
            buf.writerIndex(buf.writerIndex() - 1);
            var discarded = assertDoesNotThrow(() -> new GroundEffectSnapshotBatchS2CPacket(buf));
            assertEquals(0, discarded.epoch());
            assertTrue(discarded.entries().isEmpty());
            assertEquals(0, buf.readableBytes());
        } finally { buf.release(); }
    }

    @Test
    void trailingBytesDiscardAnOtherwiseValidSnapshotBatch() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new GroundEffectSnapshotBatchS2CPacket(8, 100, false, List.of(entry(1, 0.5))).toBytes(buf);
            buf.writeByte(1);
            var discarded = new GroundEffectSnapshotBatchS2CPacket(buf);
            assertEquals(0, discarded.epoch());
            assertTrue(discarded.entries().isEmpty());
            assertEquals(0, buf.readableBytes());
        } finally { buf.release(); }
    }

    @Test
    void nonFiniteSnapshotRadiusDiscardsTheWholeBatch() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new GroundEffectSnapshotBatchS2CPacket(8, 100, false, List.of(entry(1, 0.5))).toBytes(buf);
            // Four one-byte header fields, slot and generation, then three position doubles.
            buf.setDouble(30, Double.NaN);
            var discarded = assertDoesNotThrow(() -> new GroundEffectSnapshotBatchS2CPacket(buf));
            assertEquals(0, discarded.epoch());
            assertTrue(discarded.entries().isEmpty());
            assertEquals(0, buf.readableBytes());
        } finally { buf.release(); }
    }

    @Test
    void invalidSnapshotRejectsTheEntireBatchBeforeDispatch() {
        List<Object> sent = new ArrayList<>();
        var packet = new GroundEffectSnapshotBatchS2CPacket(8, 100, false,
                List.of(entry(1, 0.5), entry(2, Double.NaN)));
        assertDoesNotThrow(() -> PacketBatching.forEach(packet, sent::add));
        assertTrue(sent.isEmpty());
    }

    private static GroundEffectSnapshotBatchS2CPacket.Entry entry(int slot, double radius) {
        return new GroundEffectSnapshotBatchS2CPacket.Entry(slot, 1, Vec3.ZERO, radius, 0, 100,
                slot, null, ResourceLocation.parse("isaac_disaster:water"), 0x3C9BE8, slot);
    }
}
