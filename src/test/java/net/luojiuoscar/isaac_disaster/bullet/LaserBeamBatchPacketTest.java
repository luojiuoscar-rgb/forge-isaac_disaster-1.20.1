package net.luojiuoscar.isaac_disaster.bullet;

import io.netty.buffer.Unpooled;
import java.util.List;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.LaserBeamBatchS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaserBeamBatchPacketTest {
    @Test
    void roundTripsBeamGeometryAndColor() {
        LaserBeamBatchS2CPacket packet = new LaserBeamBatchS2CPacket(List.of(
                new LaserBeamBatchS2CPacket.Beam(Vec3.ZERO, new Vec3(10, 2, -1), 0.75F, 0x12ABEF)));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        packet.toBytes(buffer);
        LaserBeamBatchS2CPacket decoded = new LaserBeamBatchS2CPacket(buffer);

        assertEquals(packet.entries(), decoded.entries());
    }
    @Test
    void invalidVisualWidthsClampToZero() {
        for (float width : new float[]{-1, Float.NaN, Float.POSITIVE_INFINITY})
            assertEquals(0, new LaserBeamBatchS2CPacket.Beam(Vec3.ZERO, Vec3.ZERO, width, 0).width());
    }
}
