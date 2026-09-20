package net.luojiuoscar.isaac_disaster.bullet;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletCorrectionS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TrajectoryNetworkStateTest {
    private static final ResourceLocation ID = ResourceLocation.parse("isaac_disaster:gravity");
    private static final Vec3 ORIGIN = new Vec3(1, 2, 3);
    private static final Vec3 AXIS = new Vec3(0, 0, 1);
    private static final Vec3 RESIDUAL = new Vec3(0.25, -1.5, 0);
    private static final Vec3 VELOCITY_RESIDUAL = new Vec3(0, -0.35, 0);

    private static TrajectoryRuntime runtime() {
        var runtime = new TrajectoryRuntime(ORIGIN, AXIS, List.of(new TrajectorySpec(ID, 2)));
        runtime.anchor(new Vec3(4, 5, 6));
        runtime.distance(8.5);
        runtime.compositionOffset(RESIDUAL);
        runtime.compositionVelocityOffset(VELOCITY_RESIDUAL);
        var state = new TrajectoryRuntimeState();
        state.phase(1.25); state.stage(2); state.direction(-1); state.triggered(true);
        runtime.states().put(ID, state);
        runtime.kinematics().initialized = true;
        runtime.kinematics().initializeLaunchBasis(new Vec3(1, 1, 0));
        runtime.kinematics().distance = 6.25;
        runtime.kinematics().velocity = new Vec3(1, -0.2, 0);
        runtime.suspend();
        return runtime;
    }

    private static void assertRuntime(TrajectoryRuntime actual) {
        assertEquals(ORIGIN, actual.origin()); assertEquals(AXIS, actual.launchDirection());
        assertEquals(List.of(new TrajectorySpec(ID, 2)), actual.specs());
        assertEquals(new Vec3(4, 5, 6), actual.anchor());
        assertEquals(RESIDUAL, actual.compositionOffset());
        assertEquals(VELOCITY_RESIDUAL, actual.compositionVelocityOffset());
        assertEquals(8.5, actual.distance());
        var state = actual.states().get(ID);
        assertEquals(1.25, state.phase()); assertEquals(2, state.stage()); assertEquals(-1, state.direction());
        assertTrue(state.triggered()); assertTrue(state.path().suspended()); assertTrue(actual.suspended());
        assertEquals(6.25, actual.kinematics().distance);
        assertEquals(new Vec3(1, -0.2, 0), actual.kinematics().velocity);
        assertEquals(0, actual.kinematics().launchForward.distanceTo(new Vec3(1, 1, 0).normalize()), 1e-12);
    }

    @Test void spawnRoundTripRestoresWholeRuntimeWithoutAliasing() {
        var source = runtime();
        var packet = new BulletSpawnS2CPacket(4, 7, 3, 99, new Vec3(8, 9, 10), new Vec3(7, 9, 10),
                new Vec3(1, 0, 0), Vec3.ZERO, 1, 5, 80, 5, 64, 2, 1, 0.2, 0.3, 0, 1,
                List.of(), ResourceLocation.parse("addon:blue_flame"), ResourceLocation.parse("addon:flame"), null, false, false, 0, 0, 0, 0, source.snapshot());
        source.states().get(ID).phase(99);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.toBytes(buffer);
            var decoded = new BulletSpawnS2CPacket(buffer);
            assertEquals(0, buffer.readableBytes());
            assertRuntime(decoded.trajectorySnapshot().restore());
            ClientBulletRuntime.INSTANCE.spawn(decoded);
            var client = ClientBulletRuntime.INSTANCE.stream().get(7, 3);
            assertEquals(ResourceLocation.parse("addon:blue_flame"), client.getTypeId());
            assertEquals(ResourceLocation.parse("addon:flame"), client.getRootTypeId());
            assertRuntime(client.getTrajectoryRuntime());
            assertEquals(new Vec3(8, 9, 10), client.position());
            client.getTrajectoryRuntime().states().get(ID).phase(99);
            client.getTrajectoryRuntime().kinematics().distance = 100;
            assertRuntime(decoded.trajectorySnapshot().restore());
        } finally { ClientBulletRuntime.INSTANCE.stream().clear(); buffer.release(); }
    }

    @Test void correctionRoundTripRestoresClockAndPhaseTogetherAndClearsOldStates() {
        var stream = ClientBulletRuntime.INSTANCE.stream();
        stream.clear(); stream.acceptEpoch(1);
        var client = net.luojiuoscar.isaac_disaster.bullet.core.BulletState.builder().build();
        stream.spawn(2, 3, 0, client);
        client.getTrajectoryRuntime().distance(100);
        client.getTrajectoryRuntime().states().put(ResourceLocation.parse("test:removed"), new TrajectoryRuntimeState());
        var packet = new BulletCorrectionS2CPacket(1, 2, 3, new Vec3(10, 0, 0), Vec3.ZERO, 0.5f, runtime().snapshot());
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.toBytes(buffer);
            var decoded = new BulletCorrectionS2CPacket(buffer);
            assertEquals(0, buffer.readableBytes());
            ClientBulletRuntime.INSTANCE.correct(decoded);
            assertRuntime(client.getTrajectoryRuntime());
            assertEquals(1, client.getTrajectoryRuntime().states().size());
            assertEquals(5, client.position().x);
            client.getTrajectoryRuntime().states().get(ID).phase(77);
            assertRuntime(decoded.trajectorySnapshot().restore());
        } finally { stream.clear(); buffer.release(); }
    }
}
