package net.luojiuoscar.isaac_disaster.networking.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExplosionKnockbackS2CPacketTest {
    @Test
    void addsImpulseWithoutReplacingCurrentMovementAndFallingVelocity() {
        var packet = new ExplosionKnockbackS2CPacket(new Vec3(0.25, 0.5, -0.125));

        Vec3 result = packet.applyTo(new Vec3(1.5, -0.75, -2.0));

        assertEquals(new Vec3(1.75, -0.25, -2.125), result);
    }

    @Test
    void zeroImpulsePreservesCurrentVelocity() {
        var packet = new ExplosionKnockbackS2CPacket(Vec3.ZERO);
        Vec3 velocity = new Vec3(-0.75, -1.25, 0.5);

        assertEquals(velocity, packet.applyTo(velocity));
    }

    @Test
    void roundTripPreservesAllDoubleComponentsWithoutMotionPacketClamping() {
        Vec3 impulse = new Vec3(4.123456789012345, -0.000000000123456789, -8.987654321098765);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new ExplosionKnockbackS2CPacket(impulse).toBytes(buffer);
            var decoded = new ExplosionKnockbackS2CPacket(buffer);

            assertEquals(impulse, decoded.impulse());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
