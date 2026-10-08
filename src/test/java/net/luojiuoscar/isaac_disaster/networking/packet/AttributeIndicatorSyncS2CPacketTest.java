package net.luojiuoscar.isaac_disaster.networking.packet;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.system.attribute_indicator.AttributeSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeIndicatorSyncS2CPacketTest {
    @Test
    void roundTripKeepsEveryValueInOrderAndBaselineFlag() {
        var snapshot = new AttributeSnapshot(0.13, 7.25, 10.75, 24.5, 1.7, -2.5);
        for (boolean baseline : new boolean[]{false, true}) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                new AttributeIndicatorSyncS2CPacket(snapshot, baseline).toBytes(buffer);
                var decoded = new AttributeIndicatorSyncS2CPacket(buffer);
                assertEquals(snapshot, decoded.snapshot());
                assertEquals(baseline, decoded.baseline());
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }
    }
}
