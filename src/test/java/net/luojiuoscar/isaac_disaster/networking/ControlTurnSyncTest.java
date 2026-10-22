package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import java.util.List;
import net.luojiuoscar.isaac_disaster.bullet.client.BulletStream;
import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletTrackingBatchS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControlTurnSyncTest {
    @Test
    void observerPredictionUsesTheServerMultiplierWithoutAnInventoryLookup() {
        BulletStream stream = new BulletStream();
        Vec3 initial = new Vec3(1, 0, 0);
        BulletState bullet = stream.spawn(1, 1, 0, BulletState.builder().controllable(true)
                .velocity(initial).maxSteeringAcceleration(0.1).noGravity(true).lifetime(100).build());
        stream.applyTrackingVelocity(0, 0,
                List.of(new BulletTrackingBatchS2CPacket.TargetSample(1, new Vec3(0, 0, 64))),
                List.of(new BulletTrackingBatchS2CPacket.VelocitySample(1, 1, initial, Vec3.ZERO, 5)),
                List.of(new BulletTrackingBatchS2CPacket.Assignment(1, 1, 1, true)));

        // Tracking samples enter prediction through the existing two-tick replay delay.
        stream.tick(BulletState::position);
        stream.tick(BulletState::position);

        assertEquals(0.5, bullet.velocity().distanceTo(initial), 1e-9);
    }

    @Test
    void spawnSnapshotCarriesControlStrengthToAnObserverWithoutOwnerInventory() {
        BulletState source = BulletState.builder().controllable(true).controlTurnMultiplier(5)
                .velocity(new Vec3(1, 0, 0)).lifetime(100).build();
        var packet = new BulletSpawnS2CPacket(1, 3, 2, 10, Vec3.ZERO, Vec3.ZERO, source.velocity(), Vec3.ZERO,
                1, 0, 100, 0, 64, 2, 1, 0.1, 0.1, 0xffffff, 1, List.of(),
                ResourceLocation.parse("test:tear"), ResourceLocation.parse("test:tear"), null, false, true,
                4, 0.6, 64, 0.8, BulletSteeringMode.LIMITED, source.getTrajectoryRuntime().snapshot(), false,
                source.stickState().snapshot(), source.controlTurnMultiplier());
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientBulletRuntime client = ClientBulletRuntime.INSTANCE;
        client.clear();
        try {
            packet.toBytes(buffer);
            BulletSpawnS2CPacket decoded = new BulletSpawnS2CPacket(buffer);
            assertEquals(5, decoded.controlTurnMultiplier());
            assertEquals(0, buffer.readableBytes());
            client.spawn(decoded);
            assertEquals(5, client.stream().get(3, 2).controlTurnMultiplier());
        } finally {
            client.clear();
            buffer.release();
        }
    }

    @Test
    void trackingUpdatesRefreshStrengthAndRejectOlderStrengthSamples() {
        BulletStream stream = new BulletStream();
        BulletState bullet = stream.spawn(1, 1, 10, BulletState.builder().controllable(true)
                .velocity(new Vec3(1, 0, 0)).lifetime(100).build());
        int[] ticks = {14, 18, 14};
        double[] expected = {5, 1, 1};
        for (int i = 0; i < ticks.length; i++) {
            int tick = ticks[i];
            double multiplier = tick == 18 ? 1 : 5;
            var packet = new BulletTrackingBatchS2CPacket(0, tick, List.of(), List.of(),
                    List.of(new BulletTrackingBatchS2CPacket.VelocitySample(1, 1, bullet.velocity(), Vec3.ZERO, multiplier)));
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                packet.toBytes(buffer);
                BulletTrackingBatchS2CPacket decoded = new BulletTrackingBatchS2CPacket(buffer);
                assertEquals(multiplier, decoded.velocitySamples().get(0).controlTurnMultiplier());
                stream.applyTrackingVelocity(0, tick, decoded.targets(), decoded.velocitySamples(), decoded.assignments());
                assertEquals(0, buffer.readableBytes());
            } finally {
                buffer.release();
            }
            assertEquals(expected[i], bullet.controlTurnMultiplier());
        }
        assertEquals(1, bullet.controlTurnMultiplier());
    }
}
