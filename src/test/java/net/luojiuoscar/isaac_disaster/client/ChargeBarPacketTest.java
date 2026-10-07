package net.luojiuoscar.isaac_disaster.client;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.ChargeBarSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChargeBarPacketTest {
    @Test
    void nullIdPacketsAreSkippedBeforeAccessingTheNetworkChannel() {
        var invalid = assertDoesNotThrow(() -> new ChargeBarUpdateS2CPacket(null, true, 1f));
        assertFalse(ChargeBarSync.syncToPlayer(null, true, 1f, null));
        assertFalse(ChargeBarSync.syncToPlayer(
                ResourceLocation.fromNamespaceAndPath("test", "valid"), true, 1f, null));
        var output = new FriendlyByteBuf(Unpooled.buffer());
        try {
            assertDoesNotThrow(() -> invalid.toBytes(output));
            assertEquals(0, output.readableBytes());
        } finally {
            output.release();
        }
    }

    @Test
    void malformedPacketsAreSkippedWithoutEmittingPartialData() {
        for (int kind = 0; kind < 3; kind++) {
            var input = new FriendlyByteBuf(Unpooled.buffer());
            var output = new FriendlyByteBuf(Unpooled.buffer());
            try {
                if (kind == 1) input.writeUtf("INVALID ID");
                if (kind == 2) input.writeResourceLocation(ResourceLocation.fromNamespaceAndPath("test", "truncated"));
                var invalid = assertDoesNotThrow(() -> new ChargeBarUpdateS2CPacket(input));
                assertDoesNotThrow(() -> invalid.toBytes(output));
                assertEquals(0, output.readableBytes());
                assertFalse(invalid.isValid());
            } finally {
                input.release();
                output.release();
            }
        }
    }

    @Test
    void roundTripsIdVisibilityAndProgressForIndependentIndicators() {
        var id = ResourceLocation.fromNamespaceAndPath("test", "second_charge");
        for (boolean visible : new boolean[] { true, false }) {
            var encoded = new FriendlyByteBuf(Unpooled.buffer());
            var decoded = new FriendlyByteBuf(Unpooled.buffer());
            try {
                new ChargeBarUpdateS2CPacket(id, visible, 0.625f).toBytes(encoded);
                new ChargeBarUpdateS2CPacket(encoded).toBytes(decoded);
                assertEquals(id, decoded.readResourceLocation());
                assertEquals(visible, decoded.readBoolean());
                assertEquals(0.625f, decoded.readFloat());
                assertEquals(0, decoded.readableBytes());
            } finally {
                encoded.release();
                decoded.release();
            }
        }
    }
}
