package net.luojiuoscar.isaac_disaster.bullet.client;

import java.util.List;
import java.util.UUID;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickState;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletStickBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletTrackingBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletStickStreamTest {
    private static final UUID TARGET = new UUID(12, 34);
    private static BulletStickState.Snapshot stuck() {
        return new BulletStickState.Snapshot(BulletStickState.Phase.STUCK, 1, 0, 0, 42, TARGET, new Vec3(0, 1, 0));
    }
    @Test void attachedBulletOutlivesLocalDurationAndIgnoresFlightUpdates() {
        BulletStream stream = new BulletStream();
        BulletState state = stream.spawn(1, 2, 10, BulletState.builder().velocity(new Vec3(1, 0, 0)).lifetime(1).build());
        assertTrue(stream.applyStick(10, new BulletStickBatchS2CPacket.Entry(1, 2, new Vec3(3, 4, 5), Vec3.ZERO, Vec3.ZERO, 0, 0, stuck(), new TrajectoryRuntime(new Vec3(3, 4, 5), Vec3.ZERO, List.of()).snapshot())));
        assertFalse(stream.correct(1, 2, Vec3.ZERO, new Vec3(1, 0, 0), 1));
        stream.applyTrackingVelocity(0, 11, List.of(),
            List.of(new BulletTrackingBatchS2CPacket.VelocitySample(1, 2, new Vec3(9, 0, 0), Vec3.ZERO)),
            List.of(new BulletTrackingBatchS2CPacket.Assignment(1, 2, 5, true)));
        for (int i = 0; i < 20; i++) stream.tick(BulletState::position);
        assertSame(state, stream.get(1, 2));
        assertTrue(state.isStuck());
        assertEquals(new Vec3(3, 4, 5), state.position());
        assertEquals(Vec3.ZERO, state.velocity());
        assertEquals(0, state.trackingHandle());
    }
    private static BulletStickBatchS2CPacket.Entry transition(BulletStickState.Phase phase, int revision,
                                                             int age, double traveled) {
        Vec3 position = new Vec3(0, 5, 0);
        Vec3 velocity = phase == BulletStickState.Phase.STUCK ? Vec3.ZERO : new Vec3(1, -0.05, 0);
        return new BulletStickBatchS2CPacket.Entry(1, 2, position, velocity, Vec3.ZERO, age, traveled,
                new BulletStickState.Snapshot(phase, 20, 0, revision, 42, TARGET, Vec3.ZERO),
                new TrajectoryRuntime(position, velocity, List.of()).snapshot());
    }

    @Test void revisionsPermitReattachmentAndRejectOldCyclesAndReusedSlots() {
        BulletStream stream = new BulletStream();
        BulletState state = stream.spawn(1, 2, 10,
                BulletState.builder().stickDurationTicks(20).velocity(new Vec3(1, 0, 0)).build());
        assertTrue(stream.applyStick(11, transition(BulletStickState.Phase.STUCK, 1, 1, 1)));
        assertTrue(stream.applyStick(12, transition(BulletStickState.Phase.DETACHED, 2, 3, 1)));
        assertTrue(state.isFlying());
        assertTrue(stream.applyStick(12, transition(BulletStickState.Phase.STUCK, 3, 3, 2)));
        assertTrue(state.isStuck());
        assertFalse(stream.applyStick(13, transition(BulletStickState.Phase.DETACHED, 2, 3, 1)));
        assertFalse(stream.applyStick(11, transition(BulletStickState.Phase.STUCK, 1, 1, 1)));
        stream.despawn(1, 2);
        stream.spawn(1, 3, 14, BulletState.builder().velocity(new Vec3(1, 0, 0)).build());
        assertFalse(stream.applyStick(15, transition(BulletStickState.Phase.STUCK, 3, 3, 2)));
        assertTrue(stream.get(1, 3).isFlying());
    }

    @Test void detachedPredictionRestoresMotionCountersAndRejectsEarlierTracking() {
        BulletStream stream = new BulletStream();
        BulletState state = stream.spawn(1, 2, 10, BulletState.builder()
                .stickDurationTicks(20).velocity(new Vec3(1, 0, 0)).lifetime(200).range(64).build());
        assertTrue(stream.applyStick(11, transition(BulletStickState.Phase.STUCK, 1, 1, 1)));
        assertTrue(stream.applyStick(20, transition(BulletStickState.Phase.DETACHED, 2, 30, 4)));
        stream.applyTrackingVelocity(0, 19, List.of(), List.of(
                new BulletTrackingBatchS2CPacket.VelocitySample(1, 2, new Vec3(9, 0, 0), Vec3.ZERO)), List.of());
        assertFalse(stream.correct(1, 2, Vec3.ZERO, new Vec3(9, 0, 0), 1, 1, 0, null));
        stream.tick(BulletState::position);
        assertTrue(state.isFlying());
        assertEquals(31, state.age());
        assertEquals(1, state.position().x, 1e-9);
        assertEquals(4.95, state.position().y, 1e-9);
        assertTrue(state.traveled() > 4);
    }
    @Test void unresolvedTargetFreezesAtServerCoordinateAfterLocalFollowing() {
        BulletStream stream = new BulletStream();
        BulletState state = stream.spawn(1, 2, 10, BulletState.builder().stickDurationTicks(20).velocity(new Vec3(1, 0, 0)).build());
        Vec3 authority = new Vec3(3, 4, 5);
        stream.applyStick(11, new BulletStickBatchS2CPacket.Entry(1, 2, authority, Vec3.ZERO, Vec3.ZERO, 0, 0, stuck(), new TrajectoryRuntime(authority, Vec3.ZERO, List.of()).snapshot()));
        stream.tick(ignored -> new Vec3(30, 40, 50));
        assertEquals(new Vec3(30, 40, 50), state.position());
        assertEquals(authority, BulletClientRenderer.attachedPosition(state, 0.5F,
            ignored -> null, stream.authoritativePosition(state)));
        assertTrue(state.isStuck());
    }
    @Test void delayedAttachmentSurvivesPredictedFlightLifetimeExpiry() {
        BulletStream stream = new BulletStream();
        BulletState state = BulletState.builder().position(new Vec3(1, 2, 3))
            .velocity(new Vec3(1, 0, 0)).acceleration(new Vec3(0, 0.2, 0))
            .stickDurationTicks(20).lifetime(5).build();
        state.restoreSnapshot(state.position(), 4, 4);
        stream.spawn(1, 2, 10, state);
        for (int i = 0; i < 5; i++) stream.tick(BulletState::position);
        assertSame(state, stream.get(1, 2), "Await the delayed authoritative phase instead of expiring flight locally");
        assertTrue(state.isAlive());
        assertEquals(new Vec3(1, 2, 3), state.position());
        assertEquals(4, state.age());
        assertTrue(stream.applyStick(11, new BulletStickBatchS2CPacket.Entry(1, 2, new Vec3(3, 4, 5), Vec3.ZERO, Vec3.ZERO, 0, 0, stuck(), new TrajectoryRuntime(new Vec3(3, 4, 5), Vec3.ZERO, List.of()).snapshot())));
        assertTrue(state.isStuck());
        assertEquals(Vec3.ZERO, state.acceleration());
    }
    @Test void delayedAttachmentSurvivesPredictedFlightRangeExpiry() {
        BulletStream stream = new BulletStream();
        BulletState state = BulletState.builder().velocity(new Vec3(1, 0, 0))
            .stickDurationTicks(20).lifetime(100).range(4)
            .trajectorySpecs(List.of(new TrajectorySpec(
                ResourceLocation.parse("test:orbit"), 0))).build();
        state.restoreSnapshot(Vec3.ZERO, 3, 4);
        stream.spawn(1, 2, 10, state);
        stream.tick(BulletState::position);
        assertSame(state, stream.get(1, 2));
        assertTrue(state.isAlive());
        assertTrue(stream.applyStick(11, new BulletStickBatchS2CPacket.Entry(1, 2, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, 0, 0, stuck(), new TrajectoryRuntime(Vec3.ZERO, Vec3.ZERO, List.of()).snapshot())));
        assertTrue(state.isStuck());
    }
    @Test void ordinaryFlightStillExpiresAtPredictedLifetime() {
        BulletStream stream = new BulletStream();
        BulletState state = BulletState.builder().velocity(new Vec3(1, 0, 0)).lifetime(5).build();
        state.restoreSnapshot(Vec3.ZERO, 4, 4);
        stream.spawn(1, 2, 10, state);
        stream.tick(BulletState::position);
        assertNull(stream.get(1, 2));
        assertFalse(state.isAlive());
    }
    @Test void lateObserverDetachedSpawnRejectsCorrectionsFromBeforeTheTransition() {
        BulletStream stream = new BulletStream();
        BulletState state = BulletState.builder().velocity(new Vec3(1, 0, 0)).range(64).build();
        state.applyStickSnapshot(new BulletStickState.Snapshot(BulletStickState.Phase.DETACHED,
                20, 5, 2, -1, null, Vec3.ZERO));
        state.restoreSnapshot(Vec3.ZERO, 50, 5);
        stream.spawn(1, 2, 100, state);
        assertFalse(stream.correct(1, 2, new Vec3(9, 0, 0), new Vec3(9, 0, 0), 1,
                10, 0, new TrajectoryRuntime(Vec3.ZERO, new Vec3(1, 0, 0), List.of()).snapshot()));
        assertEquals(50, state.age());
        assertEquals(5, state.traveled());
        assertEquals(Vec3.ZERO, state.position());
    }
}
