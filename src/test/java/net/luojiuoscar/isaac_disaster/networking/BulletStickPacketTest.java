package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickState;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletStickBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletStickPacketTest {
    @Test void lateObserverSpawnInstallsStuckPhaseBeforeZeroVelocityValidation() {
        var snapshot = new BulletStickState.Snapshot(BulletStickState.Phase.STUCK, 80, 40, 0,
            37, new UUID(1, 2), new Vec3(0, 1, 0));
        var origin = new Vec3(4, 5, 6);
        var packet = new BulletSpawnS2CPacket(
            4, 3, 2, 100, origin, origin, Vec3.ZERO, Vec3.ZERO,
            1, 100, 1, 50, 1, 2, 1, 0.1, 0.1, 0xffffff, 1,
            List.of(), ResourceLocation.parse("test:tear"),
            ResourceLocation.parse("test:tear"), null, true, true,
            10, 1, 10, 1, BulletSteeringMode.LIMITED,
            new TrajectoryRuntime(origin, Vec3.ZERO, List.of()).snapshot(),
            false, snapshot);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        var client = ClientBulletRuntime.INSTANCE;
        client.clear();
        try {
            packet.toBytes(buffer);
            var decoded = new BulletSpawnS2CPacket(buffer);
            assertEquals(snapshot, decoded.stickSnapshot());
            client.spawn(decoded);
            var state = client.stream().get(3, 2);
            assertNotNull(state);
            assertTrue(state.isStuck());
            assertTrue(state.isAlive());
            client.stream().tick(BulletState::position);
            assertSame(state, client.stream().get(3, 2));
        } finally { client.clear(); buffer.release(); }
    }

    @Test void roundTripPreservesTargetIdentityAndPhaseCounters() {
        var snapshot = new BulletStickState.Snapshot(BulletStickState.Phase.STUCK, 100, 17, 0,
            37, new UUID(1, 2), new Vec3(0.25, 1.5, -0.75));
        var entry = new BulletStickBatchS2CPacket.Entry(3, 9, new Vec3(8, 9, 10), Vec3.ZERO, Vec3.ZERO, 0, 0, snapshot, new TrajectoryRuntime(new Vec3(8, 9, 10), Vec3.ZERO, List.of()).snapshot());
        var original = new BulletStickBatchS2CPacket(5, 123, List.of(entry));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            original.toBytes(buffer);
            var decoded = new BulletStickBatchS2CPacket(buffer);
            assertEquals(5, decoded.epoch());
            assertEquals(123, decoded.serverTick());
            assertEquals(1, decoded.entries().size());
            var restored = decoded.entries().get(0);
            assertEquals(entry.stickSnapshot(), restored.stickSnapshot());
            assertEquals(entry.position(), restored.position());
            assertEquals(entry.velocity(), restored.velocity());
            assertEquals(entry.acceleration(), restored.acceleration());
            assertEquals(entry.age(), restored.age());
            assertEquals(entry.traveled(), restored.traveled());
            assertEquals(entry.trajectorySnapshot().restore().specs(), restored.trajectorySnapshot().restore().specs());
            assertEquals(0, buffer.readableBytes());
        } finally { buffer.release(); }
    }
}
