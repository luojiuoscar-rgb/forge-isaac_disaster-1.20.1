package net.luojiuoscar.isaac_disaster.bullet.client;

import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletTrackingBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulletStreamTest {
    @Test
    void staleGenerationCannotBeCorrectedAfterDespawn() {
        BulletStream stream = new BulletStream();
        BulletState state = stream.spawn(2, 7, 10, BulletState.builder().position(Vec3.ZERO).build());
        stream.despawn(2, 7);
        assertEquals(0, stream.size());
        stream.correct(2, 7, new Vec3(5, 0, 0), Vec3.ZERO, 1.0);
        assertEquals(Vec3.ZERO, state.position());
    }

    @Test
    void correctionWithoutTrajectorySnapshotCannotDesynchronizeModuleState() {
        BulletStream stream = new BulletStream();
        BulletState state = stream.spawn(
            8,
            1,
            0,
            BulletState.builder()
                .position(Vec3.ZERO)
                .velocity(new Vec3(1, 0, 0))
                .trajectorySpecs(List.of(new TrajectorySpec(ResourceLocation.parse("test:orbit"), 0)))
                .build());
        var kinematics = state.getTrajectoryRuntime().kinematics();
        kinematics.initialized(true);
        kinematics.position(new Vec3(-3, 0, 0));
        kinematics.velocity(new Vec3(1, 0, 0));
        kinematics.offset(new Vec3(2, 0, 0));

        assertFalse(stream.correct(
            8,
            1,
            new Vec3(10, 2, 0),
            new Vec3(0, 0, 1),
            1.0,
            4,
            4.0,
            null));

        assertEquals(Vec3.ZERO, state.position());
        assertEquals(new Vec3(-3, 0, 0), kinematics.position());
        assertEquals(new Vec3(2, 0, 0), kinematics.offset());
        assertEquals(0, state.age());
    }

    @Test
    void olderCorrectionCannotOverwriteNewerPerBulletSnapshot() {
        BulletStream stream = new BulletStream();
        BulletState state = stream.spawn(
            9,
            1,
            0,
            BulletState.builder().position(Vec3.ZERO).velocity(new Vec3(1, 0, 0)).build());

        assertTrue(stream.correct(9, 1, new Vec3(5, 0, 0), new Vec3(0.1, 0, 0), 1.0, 5, 5.0, null));
        assertFalse(stream.correct(9, 1, new Vec3(2, 0, 0), Vec3.ZERO, 1.0, 4, 4.0, null));
        assertFalse(stream.correct(9, 1, new Vec3(3, 0, 0), Vec3.ZERO, 1.0, 5, 4.0, null));
        assertFalse(stream.correct(9, 1, new Vec3(4, 0, 0), Vec3.ZERO, 1.0, 5, 5.0, null));
        assertEquals(new Vec3(5, 0, 0), state.position());
        assertEquals(5, state.age());
        assertEquals(5.0, state.traveled(), 1e-9);
    }

    @Test
    void temporaryLowSpeedGuardRemovesClientStateFromAnInvalidCorrection() {
        BulletStream stream = new BulletStream();
        stream.spawn(10, 1, 0, BulletState.builder().velocity(new Vec3(1, 0, 0)).build());

        assertFalse(stream.correct(
            10,
            1,
            Vec3.ZERO,
            new Vec3(BulletState.MIN_VALID_SPEED - 1.0E-6D, 0, 0),
            1.0,
            1,
            1.0,
            null));
        assertEquals(0, stream.size());
    }

    @Test
    void temporaryLowSpeedGuardRemovesClientStateDuringPrediction() {
        BulletStream stream = new BulletStream();
        stream.spawn(
            11,
            1,
            0,
            BulletState.builder()
                .velocity(new Vec3(BulletState.MIN_VALID_SPEED - 1.0E-6D, 0, 0))
                .lifetime(20)
                .build());

        stream.tick();

        assertEquals(0, stream.size());
    }

    @Test
    void spawnSnapshotIsNotFastForwardedByClientLocalTicks() {
        BulletStream stream = new BulletStream();
        stream.spawn(1, 1, 10, BulletState.builder().lifetime(20).build());
        stream.tick();
        stream.tick();
        stream.tick();

        BulletState late = stream.spawn(2, 1, 10, BulletState.builder()
                .position(Vec3.ZERO).velocity(new Vec3(1, 0, 0)).lifetime(20).build());

        assertEquals(0.0, late.position().x, 1e-9);
        assertEquals(0, late.age());
    }

    @Test
    void newWorldSpawnDoesNotUseThePreviousWorldTickBaseline() {
        BulletStream stream = new BulletStream();
        stream.acceptEpoch(1);
        stream.spawn(1, 1, 1_000, BulletState.builder().lifetime(5).build());
        stream.acceptEpoch(2);

        BulletState fresh = stream.spawn(2, 1, 0, BulletState.builder()
                .position(Vec3.ZERO).velocity(new Vec3(1, 0, 0)).lifetime(20).build());

        assertEquals(1, stream.size());
        assertEquals(0, fresh.age());
        assertEquals(0.0, fresh.position().x, 1e-9);
    }

    @Test
    void olderEpochCannotClearTheCurrentWorldStream() {
        BulletStream stream = new BulletStream();
        assertTrue(stream.acceptEpoch(8));
        BulletState live = stream.spawn(1, 2, 0, BulletState.builder().lifetime(20).build());

        assertFalse(stream.acceptEpoch(7));
        assertEquals(8, stream.epoch());
        assertEquals(1, stream.size());
        assertSame(live, stream.get(1, 2));
    }

    @Test
    void worldUnloadRetainsEpochWatermarkButClearsState() {
        BulletStream stream = new BulletStream();
        stream.acceptEpoch(12);
        stream.spawn(4, 1, 0, BulletState.builder().lifetime(20).build());

        stream.clearForWorldUnload();

        assertEquals(12, stream.epoch());
        assertEquals(0, stream.size());
        assertFalse(stream.acceptEpoch(11));
        assertTrue(stream.acceptEpoch(13));
    }

    @Test
    void duplicateOrOlderSpawnCannotReplaceTheCurrentSlotGeneration() {
        BulletStream stream = new BulletStream();
        BulletState current = stream.spawn(6, 4, 0, BulletState.builder().position(new Vec3(4, 0, 0)).build());

        assertNull(stream.spawn(6, 4, 1, BulletState.builder().position(new Vec3(9, 0, 0)).build()));
        assertNull(stream.spawn(6, 3, 1, BulletState.builder().position(new Vec3(9, 0, 0)).build()));
        assertSame(current, stream.get(6, 4));
        assertEquals(4.0D, current.position().x, 1.0E-9D);
    }

    @Test
    void trackingVelocitySampleBlendsInsteadOfTurningInstantly() {
        BulletStream stream = new BulletStream();
        stream.acceptEpoch(4);
        BulletState state = stream.spawn(3, 1, 3, BulletState.builder()
                .velocity(new Vec3(1, 0, 0)).homing(true).homingSteer(1.0).lifetime(20).build());
        stream.applyTrackingVelocity(4, 3, List.of(), List.of(
                new BulletTrackingBatchS2CPacket.VelocitySample(3, 1, new Vec3(0, 0, 1), Vec3.ZERO)),
                List.of());

        assertEquals(1.0, state.velocity().x, 1e-9);
        stream.tick();
        assertTrue(state.velocity().x > 0.0);
        assertTrue(state.velocity().z > 0.0);
        assertTrue(state.velocity().z < 1.0);
    }

    @Test
    void directTrackingUsesItsTargetAndTreatsZeroVelocityAsAConstrainedCorrection() {
        BulletStream stream = new BulletStream();
        stream.acceptEpoch(5);
        BulletState fetus = stream.spawn(4, 1, 3, BulletState.builder().position(Vec3.ZERO)
                .velocity(new Vec3(1, 0, 0)).homing(true).attackType(
                        TestAttackTypes.C_SECTION)
                .steeringMode(BulletSteeringMode.DIRECT).lifetime(20).build());

        stream.applyTrackingVelocity(5, 3, List.of(
                        new BulletTrackingBatchS2CPacket.TargetSample(1, new Vec3(0, 0, 3))),
                List.of(new BulletTrackingBatchS2CPacket.VelocitySample(4, 1, Vec3.ZERO, Vec3.ZERO)),
                List.of(new BulletTrackingBatchS2CPacket.Assignment(4, 1, 1, false)));

        stream.tick();
        stream.tick();

        assertTrue(fetus.velocity().length() > 0.0D);
        assertTrue(fetus.position().z > 0.0D);
        assertTrue(fetus.position().x > 0.0D && fetus.position().x < 1.0D);
    }

    @Test
    void despawnClearsAllIdentityMetadata() {
        BulletStream stream = new BulletStream();
        stream.acceptEpoch(9);
        stream.spawn(7, 1, 0, BulletState.builder().homing(true).lifetime(20).build());
        stream.applyTrackingVelocity(9, 4, List.of(), List.of(
                new BulletTrackingBatchS2CPacket.VelocitySample(7, 1, Vec3.ZERO, Vec3.ZERO)), List.of());

        assertTrue(stream.identityMetadataSize() > 0);
        stream.despawn(7, 1);

        assertEquals(0, stream.identityMetadataSize());
    }

    @Test
    void replacingSlotGenerationClearsOldIdentityMetadata() {
        BulletStream stream = new BulletStream();
        stream.acceptEpoch(10);
        stream.spawn(3, 1, 0, BulletState.builder().homing(true).lifetime(20).build());
        stream.applyTrackingVelocity(10, 2, List.of(), List.of(
                new BulletTrackingBatchS2CPacket.VelocitySample(3, 1, Vec3.ZERO, Vec3.ZERO)), List.of());

        BulletState current = stream.spawn(3, 2, 0, BulletState.builder().homing(true).lifetime(20).build());

        assertEquals(0, stream.identityMetadataSize());
        assertFalse(stream.despawn(3, 1));
        assertSame(current, stream.get(3, 2));
    }

    @Test
    void sharedTrackingHandleRemainsUsableAfterOneBulletDespawns() {
        BulletStream stream = new BulletStream();
        stream.acceptEpoch(12);
        BulletState survivor = stream.spawn(2, 1, 3, BulletState.builder()
            .position(Vec3.ZERO)
            .velocity(new Vec3(1, 0, 0))
            .homing(true)
            .steeringMode(BulletSteeringMode.DIRECT)
            .lifetime(20)
            .build());
        stream.spawn(1, 1, 3, BulletState.builder().homing(true).lifetime(20).build());
        stream.applyTrackingVelocity(12, 3,
            List.of(new BulletTrackingBatchS2CPacket.TargetSample(7, new Vec3(0, 0, 3))),
            List.of(),
            List.of(
                new BulletTrackingBatchS2CPacket.Assignment(1, 1, 7, false),
                new BulletTrackingBatchS2CPacket.Assignment(2, 1, 7, false)));

        assertTrue(stream.despawn(1, 1));
        stream.applyTrackingVelocity(12, 4,
            List.of(new BulletTrackingBatchS2CPacket.TargetSample(7, new Vec3(0, 0, 4))),
            List.of(), List.of());
        stream.tick();
        stream.tick();

        assertTrue(survivor.position().z > 0.0D);
    }

    @Test
    void lifetimeExpiryClearsIdentityMetadata() {
        BulletStream stream = new BulletStream();
        stream.acceptEpoch(11);
        stream.spawn(5, 1, 0, BulletState.builder().homing(true).lifetime(1).build());
        stream.applyTrackingVelocity(11, 2, List.of(), List.of(
                new BulletTrackingBatchS2CPacket.VelocitySample(5, 1, Vec3.ZERO, Vec3.ZERO)), List.of());

        stream.tick();

        assertEquals(0, stream.size());
        assertEquals(0, stream.identityMetadataSize());
    }

    @Test
    void visitorSeesEachActiveStateWithoutChangingStreamSize() {
        BulletStream stream = new BulletStream();
        stream.spawn(1, 1, 0, BulletState.builder().build());
        stream.spawn(2, 1, 0, BulletState.builder().build());
        List<BulletState> visited = new ArrayList<>();

        stream.forEachState(visited::add);

        assertEquals(2, visited.size());
        assertEquals(2, stream.size());
    }
}
