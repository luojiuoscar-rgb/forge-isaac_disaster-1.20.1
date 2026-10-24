package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeSpawnDataTest {
    @Test
    void aDetachedShotCarriesItsRealDirectionAndOwnerInTheInitialPayload() {
        var source = new PlayerMomsKnifeSpawnData((byte) 1, 765, new Vec3(-0.3, 0.5, -0.8), 24, 1.7f, 0, 159, -32);
        checkRoundTrip(source);
    }

    @Test
    void aMainKnifeCarriesItsFlightClockAndVerticalHeading() {
        var source = new PlayerMomsKnifeSpawnData((byte) 0, 42, new Vec3(0, 1, 0), 6, 0.5f, 872, 37, -90);
        checkRoundTrip(source);
    }

    private static void checkRoundTrip(PlayerMomsKnifeSpawnData source) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            source.write(buffer);
            var result = PlayerMomsKnifeSpawnData.read(buffer);
            assertEquals(source.mode(), result.mode());
            assertEquals(source.owner(), result.owner());
            assertTrue(source.direction().distanceTo(result.direction()) < 1e-12);
            assertEquals(source.range(), result.range());
            assertEquals(source.speed(), result.speed());
            assertEquals(source.launchedAt(), result.launchedAt());
            assertEquals(source.yaw(), result.yaw());
            assertEquals(source.pitch(), result.pitch());
            assertEquals(0, buffer.readableBytes());
        } finally { buffer.release(); }
    }
}
