package net.luojiuoscar.isaac_disaster.bullet;

import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletManager;
import net.luojiuoscar.isaac_disaster.bullet.tracking.TrackingProfile;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletStateTest {
    @Test
    void launchFrameUsesFinalSpawnPositionAndVelocity() {
        Vec3 spawn = new Vec3(3, 5, 7);
        Vec3 velocity = new Vec3(0, 2, 3);
        BulletState state = BulletState.builder().position(spawn).velocity(velocity).build();
        assertEquals(spawn, state.getTrajectoryRuntime().origin());
        assertEquals(velocity.normalize(), state.getTrajectoryRuntime().launchDirection());
        state.setPosition(spawn.add(10, 0, 0));
        state.setVelocity(new Vec3(1, 0, 0));
        assertEquals(spawn, state.getTrajectoryRuntime().origin());
        assertEquals(velocity.normalize(), state.getTrajectoryRuntime().launchDirection());
    }

    @Test
    void integratesVelocityAccelerationAndDistance() {
        BulletState state = BulletState.builder()
                .position(Vec3.ZERO)
                .velocity(new Vec3(1, 0, 0))
                .acceleration(new Vec3(0.5, 0, 0))
                .lifetime(3)
                .build();

        assertTrue(state.tickPhysics());
        assertEquals(1.5, state.position().x, 1e-9);
        assertEquals(1.5, state.traveled(), 1e-9);
        assertEquals(Vec3.ZERO, state.previousPosition());

        assertTrue(state.tickPhysics());
        assertEquals(3.5, state.position().x, 1e-9);
        assertEquals(3.5, state.traveled(), 1e-9);
        assertFalse(state.tickPhysics());
        assertEquals(3.5, state.position().x, 1e-9);
        assertEquals(3, state.age());
    }

    @Test
    void slotGenerationChangesWhenReused() {
        BulletManager manager = new BulletManager();
        BulletState first = manager.spawn(BulletState.builder().build());
        int slot = first.slot();
        int generation = first.generation();
        manager.remove(first);

        BulletState second = manager.spawn(BulletState.builder().build());
        assertEquals(slot, second.slot());
        assertTrue(second.generation() > generation);
        assertFalse(manager.remove(first));
        assertEquals(1, manager.activeCount());
    }

    @Test
    void expiredStateIsDeferredThenRemovedFromTheActiveSlots() {
        BulletManager manager = new BulletManager();
        BulletState state = manager.spawn(BulletState.builder().lifetime(1).build());

        manager.tick();

        assertEquals(0, manager.activeCount());
        assertNull(manager.get(state.slot(), state.generation()));
    }

    @Test
    void clientCorrectionIsBlendedInsteadOfSnapping() {
    BulletState state = BulletState.builder().position(Vec3.ZERO).velocity(new Vec3(0.1, 0, 0)).build();
    state.applyCorrection(new Vec3(10, 0, 0), new Vec3(0.1, 0, 0), 0.25);
    assertEquals(10, state.position().x, 1e-9);
    assertEquals(2.5, state.previousPosition().x, 1e-9);
    }

    @Test
    void fetusHitCooldownKeepsTheTargetBlockedForSixTicks() {
        BulletState state = BulletState.builder().velocity(new Vec3(0.1, 0, 0)).lifetime(10).build();
        state.setHitCooldownTicks(6);

        for (int tick = 0; tick < 5; tick++) state.tickPhysics();

        assertEquals(1, state.hitCooldownTicks());
        state.tickPhysics();
        assertEquals(0, state.hitCooldownTicks());
    }

    @Test
    void temporaryLowSpeedGuardRejectsInvalidVelocitiesButKeepsTheBoundary() {
        BulletState state = BulletState.builder().velocity(new Vec3(1, 0, 0)).build();

        state.setVelocity(new Vec3(BulletState.MIN_VALID_SPEED, 0, 0));
        assertTrue(state.isAlive());

        state.setVelocity(new Vec3(BulletState.MIN_VALID_SPEED - 1.0E-6D, 0, 0));
        assertFalse(state.isAlive());

        BulletState nonFinite = BulletState.builder().velocity(new Vec3(1, 0, 0)).build();
        nonFinite.setVelocity(new Vec3(Double.NaN, 0, 0));
        assertFalse(nonFinite.isAlive());
    }

    @Test
    void temporaryLowSpeedGuardRemovesInvalidStatesFromTheServerManager() {
        BulletManager manager = new BulletManager();
        BulletState state = manager.spawn(
            BulletState.builder().velocity(new Vec3(BulletState.MIN_VALID_SPEED - 1.0E-6D, 0, 0)).build());

        manager.tick();

        assertFalse(state.isAlive());
        assertEquals(0, manager.activeCount());
        assertEquals(1, manager.drainDespawns().size());
    }

    @Test
    void visualScaleAndCollisionExtentsRemainIndependent() {
        BulletState state = BulletState.builder().renderScale(2.0).collisionWidth(0.4).collisionHeight(1.2).build();

        assertEquals(2.0, state.renderScale(), 1e-9);
        assertEquals(0.4, state.collisionWidth(), 1e-9);
        assertEquals(1.2, state.collisionHeight(), 1e-9);
        assertEquals(1.2F, state.getCollisionHeight(), 1e-6F);
    }

    @Test
    void pushOutOfBlockClearsTheCollisionHalfExtent() {
        BulletState state = BulletState.builder().position(Vec3.ZERO)
                .collisionWidth(0.2).collisionHeight(0.6).build();

        state.pushOutOfBlock(new Vec3(-1, 0, 0));

        assertEquals(-0.1001, state.position().x, 1e-9);
    }

    @Test
    void setCenterKeepsLightweightPositionInCenterCoordinates() {
        net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject bullet =
                BulletState.builder().position(Vec3.ZERO).collisionHeight(1.2D).build();

        bullet.setCenter(new Vec3(2.0D, 3.0D, 4.0D));

        assertEquals(new Vec3(2.0D, 3.0D, 4.0D), bullet.getCenter());
        assertEquals(new Vec3(2.0D, 3.0D, 4.0D), bullet.getPosition());
    }

    @Test
    void steeringApproachesDesiredVelocityWithinTheConfiguredAccelerationLimit() {
        BulletState state = BulletState.builder().velocity(new Vec3(1, 0, 0))
                .maxSteeringAcceleration(0.25).maxSpeedChange(0.25).build();

        state.setDesiredVelocity(new Vec3(0, 0, 1));
        state.applySteering();

        assertTrue(state.velocity().distanceTo(new Vec3(1, 0, 0)) <= 0.2500001D);
        assertTrue(state.velocity().z > 0.0D);
        assertTrue(state.velocity().x > 0.0D);
    }

    @Test
    void snapshotRestoresAuthoritativeAgePreviousPositionAndDistance() {
        BulletState state = BulletState.builder().position(new Vec3(4, 2, 1)).build();

        state.restoreSnapshot(new Vec3(3, 2, 1), 7, 12.5D);

        assertEquals(new Vec3(3, 2, 1), state.previousPosition());
        assertEquals(7, state.age());
        assertEquals(12.5D, state.traveled(), 1e-9);
    }

    @Test
    void trackingProfileBiasesCandidateSearchWithoutChangingBulletPosition() {
        BulletState state = BulletState.builder().position(new Vec3(2, 3, 4))
                .velocity(new Vec3(1, 0, 0)).homingRange(8.0D).build();

        TrackingProfile profile = TrackingProfile.forBullet(state);

        assertEquals(new Vec3(5, 3, 4), profile.searchCenter(state));
        assertEquals(new Vec3(2, 3, 4), state.position());
    }

    @Test
    void fetusTrackingProfileUsesACommonProfileWithStrongerSteering() {
        BulletState normal = BulletState.builder().homingRange(6.0D).homingSteer(0.6D).build();
        BulletState fetus = BulletState.builder().attackType(
                TestAttackTypes.C_SECTION)
                .homingRange(6.0D).homingSteer(0.6D).build();

        assertEquals(0.6D, TrackingProfile.forBullet(normal).steer(), 1.0E-9D);
        assertTrue(TrackingProfile.forBullet(fetus).steer() > TrackingProfile.forBullet(normal).steer());
        assertEquals(0.25D, TrackingProfile.forBullet(fetus).minHomingSpeed(), 1.0E-9D);
    }

    @Test
    void fetusUsesDirectSteeringAndDoesNotRememberHitTargets() {
        BulletState fetus = BulletState.builder().position(Vec3.ZERO).velocity(new Vec3(1, 0, 0))
                .baseSpeed(1.0D).attackType(
                        TestAttackTypes.C_SECTION)
                .steeringMode(BulletSteeringMode.DIRECT).rememberHitTargets(false).build();

        fetus.applyDirectSteering(new Vec3(0, 0, 3), 0.5D);

        assertFalse(fetus.rememberHitTargets());
        assertEquals(BulletSteeringMode.DIRECT, fetus.steeringMode());
        assertEquals(new Vec3(0, 0, 0.5D), fetus.velocity());
    }

    @Test
    void directTrackingKeepsCoordinateHeadingWhenAuthorityReportsZeroVelocity() {
        BulletState fetus = BulletState.builder().position(Vec3.ZERO).velocity(new Vec3(1, 0, 0))
                .attackType(TestAttackTypes.C_SECTION)
                .steeringMode(BulletSteeringMode.DIRECT).build();

        fetus.applyDirectTrackingSample(new Vec3(0, 1, 0), Vec3.ZERO, false);

        assertTrue(fetus.velocity().y > 0.0D);
        assertTrue(fetus.velocity().length() < 1.0D);
    }

    @Test
    void ordinaryHomingBrakesNearTargetAndRecoversItsBaseSpeedWithoutTarget() {
        BulletState tear = BulletState.builder().position(Vec3.ZERO).velocity(new Vec3(1, 0, 0))
                .baseSpeed(1.0D).homingRange(4.0D).maxSteeringAcceleration(1.0D).maxSpeedChange(0.1D).build();
        TrackingProfile profile = TrackingProfile.forBullet(tear);

        Vec3 brakingVelocity = profile.limitedHomingVelocity(tear, new Vec3(0.5D, 0, 0));
        assertTrue(brakingVelocity.length() < tear.baseSpeed());

        tear.setDesiredVelocity(brakingVelocity);
        tear.applySteering();
        assertEquals(0.9D, tear.velocity().length(), 1.0E-9D);

        tear.setDesiredVelocity(profile.cruiseVelocity(tear));
        tear.applySteering();
        assertEquals(1.0D, tear.velocity().length(), 1.0E-9D);
    }

    @Test
    void blockContactsAreExactAndDefensivelyCopied() {
        BlockPos mutable = new BlockPos(1, 2, 3);
        BulletState state = BulletState.builder().build();

        assertTrue(state.markBlockHit(mutable));
        assertFalse(state.markBlockHit(new BlockPos(1, 2, 3)));
        assertFalse(state.markBlockHit(null));
        assertEquals(java.util.Set.of(new BlockPos(1, 2, 3)), state.getHitBlockPositions());
        assertThrows(UnsupportedOperationException.class,
                () -> state.getHitBlockPositions().clear());
    }

}
