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
        var invalid = assertDoesNotThrow(() -> new ChargeBarUpdateS2CPacket(null, ChargeBarUpdateS2CPacket.Action.END, true, 1f, 0f));
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
                new ChargeBarUpdateS2CPacket(id, ChargeBarUpdateS2CPacket.Action.END, visible, 0.625f, 0f).toBytes(encoded);
                new ChargeBarUpdateS2CPacket(encoded).toBytes(decoded);
                assertEquals(id, decoded.readResourceLocation());
                assertEquals(ChargeBarUpdateS2CPacket.Action.END, decoded.readEnum(ChargeBarUpdateS2CPacket.Action.class));
                assertEquals(visible, decoded.readBoolean());
                assertEquals(0.625f, decoded.readFloat());
                assertEquals(0f, decoded.readFloat());
                assertEquals(0, decoded.readableBytes());
            } finally {
                encoded.release();
                decoded.release();
            }
        }
    }

    @Test
    void roundTripsEveryActionAndItsPredictionRate() {
        var id = ResourceLocation.fromNamespaceAndPath("test", "predicted_charge");
        for (var action : ChargeBarUpdateS2CPacket.Action.values()) {
            var encoded = new FriendlyByteBuf(Unpooled.buffer());
            var decoded = new FriendlyByteBuf(Unpooled.buffer());
            try {
                new ChargeBarUpdateS2CPacket(id, action, true, 0.25f, 1f / 47).toBytes(encoded);
                var packet = new ChargeBarUpdateS2CPacket(encoded);
                assertTrue(packet.isValid());
                packet.toBytes(decoded);
                assertEquals(id, decoded.readResourceLocation());
                assertEquals(action, decoded.readEnum(ChargeBarUpdateS2CPacket.Action.class));
                assertTrue(decoded.readBoolean());
                assertEquals(0.25f, decoded.readFloat());
                assertEquals(1f / 47, decoded.readFloat());
                assertEquals(0, decoded.readableBytes());
            } finally {
                encoded.release();
                decoded.release();
            }
        }
    }

    @Test
    void rejectsInvalidActionTruncatedRateAndNonFiniteValues() {
        var id = ResourceLocation.fromNamespaceAndPath("test", "invalid_charge");
        for (int kind = 0; kind < 4; kind++) {
            var input = new FriendlyByteBuf(Unpooled.buffer());
            try {
                input.writeResourceLocation(id);
                input.writeVarInt(kind == 0 ? 99 : ChargeBarUpdateS2CPacket.Action.START.ordinal());
                input.writeBoolean(true);
                input.writeFloat(kind == 2 ? Float.NaN : 0.25f);
                if (kind != 1) input.writeFloat(kind == 3 ? -1f : 0.01f);
                assertFalse(new ChargeBarUpdateS2CPacket(input).isValid());
            } finally {
                input.release();
            }
        }
    }
}
