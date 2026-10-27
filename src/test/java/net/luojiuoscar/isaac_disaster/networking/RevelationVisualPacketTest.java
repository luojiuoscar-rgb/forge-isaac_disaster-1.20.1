package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.RevelationBeamS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RevelationVisualPacketTest {
    private static final ResourceLocation DIMENSION = ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether");

    @Test
    void beamRoundTripPreservesAgeAndExistingControlFlags() {
        for (int age : new int[]{0, 2, 20, 28}) {
            var expected = new RevelationBeamS2CPacket(UUID.randomUUID(), UUID.randomUUID(), 123, DIMENSION,
                    new Vec3(1, 2, 3), new Vec3(.3, .4, .5), 64, 1.25F, age == 28, true, false, age);
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                expected.toBytes(buffer);
                assertEquals(expected, new RevelationBeamS2CPacket(buffer));
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
        }
    }
}
