package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import net.luojiuoscar.isaac_disaster.networking.packet.holylight.HolyLightSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HolyLightPacketTest {
    private static final ResourceLocation DIMENSION = ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");

    @Test
    void roundTripPreservesWorldTimelineAndRemoval() {
        var expected = new HolyLightSyncS2CPacket(7, DIMENSION, 105, false,
                List.of(snapshot(1)), List.of(6L));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            expected.toBytes(buffer);
            assertEquals(expected, new HolyLightSyncS2CPacket(buffer));
            assertEquals(0, buffer.readableBytes());
        } finally { buffer.release(); }
    }

    @Test
    void truncatedPacketCannotInstallPartialSnapshots() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new HolyLightSyncS2CPacket(7, DIMENSION, 105, false,
                    List.of(snapshot(1), snapshot(2)), List.of()).toBytes(buffer);
            buffer.writerIndex(buffer.writerIndex() - 1);
            var decoded = assertDoesNotThrow(() -> new HolyLightSyncS2CPacket(buffer));
            assertEquals(0, decoded.epoch());
            assertTrue(decoded.entries().isEmpty());
        } finally { buffer.release(); }
    }

    @Test
    void trailingBytesAndNonFiniteGeometryAreRejected() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new HolyLightSyncS2CPacket(7, DIMENSION, 105, false,
                    List.of(snapshot(1)), List.of()).toBytes(buffer);
            buffer.writeByte(1);
            assertEquals(0, new HolyLightSyncS2CPacket(buffer).epoch());
            buffer.clear();
            new HolyLightSyncS2CPacket(7, DIMENSION, 105, false,
                    List.of(new HolyLightSnapshot(1, Vec3.ZERO, Double.NaN, 16, 100, 0, false, 4)),
                    List.of()).toBytes(buffer);
            assertEquals(0, new HolyLightSyncS2CPacket(buffer).epoch());
        } finally { buffer.release(); }
    }

    @Test
    void batchingPreservesEveryEntryAndResetsTheSessionOnlyOnce() {
        var entries = IntStream.rangeClosed(1, 12_000).mapToObj(HolyLightPacketTest::snapshot).toList();
        List<Object> pieces = new ArrayList<>();
        PacketBatching.forEach(new HolyLightSyncS2CPacket(7, DIMENSION, 105, true, entries, List.of()), pieces::add);
        assertTrue(pieces.size() > 1);
        int resets = 0;
        List<HolyLightSnapshot> delivered = new ArrayList<>();
        for (Object piece : pieces) {
            var packet = assertInstanceOf(HolyLightSyncS2CPacket.class, piece);
            if (packet.reset()) resets++;
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                packet.toBytes(buffer);
                assertTrue(buffer.readableBytes() <= 512 * 1024);
                var decoded = new HolyLightSyncS2CPacket(buffer);
                assertEquals(7, decoded.epoch());
                delivered.addAll(decoded.entries());
            } finally { buffer.release(); }
        }
        assertEquals(1, resets);
        assertEquals(entries, delivered);
    }

    private static HolyLightSnapshot snapshot(long id) {
        return new HolyLightSnapshot(id, new Vec3(1.5, 64.002, -2.5), .8, 12, 100, 18, id % 2 == 0, 99);
    }

    @Test
    void invalidWarningDurationIsRejectedBeforeClientStateCanBeInstalled() {
        for (int duration : new int[]{-1, 701, Integer.MAX_VALUE}) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                new HolyLightSyncS2CPacket(7, DIMENSION, 105, false,
                        List.of(new HolyLightSnapshot(1, Vec3.ZERO, .8, 16, 100, duration, false, 4)),
                        List.of()).toBytes(buffer);
                assertEquals(0, new HolyLightSyncS2CPacket(buffer).epoch());
            } finally { buffer.release(); }
        }
    }
}
