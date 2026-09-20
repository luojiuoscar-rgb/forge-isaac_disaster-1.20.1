package net.luojiuoscar.isaac_disaster.bullet;

import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;

class MyReflectionTrajectoryTest {
    private static final Map<ResourceLocation, TrajectoryModule> MODULES = Map.of(
            MyReflectionLaserTrajectoryModule.ID, new MyReflectionLaserTrajectoryModule(),
            MyReflectionBulletTrajectoryModule.ID, new MyReflectionBulletTrajectoryModule(),
            TinyPlanetLaserTrajectoryModule.ID, new TinyPlanetLaserTrajectoryModule());

    private static final class Shot {
        final BulletState bullet;
        final Vec3 axis;
        Vec3 anchor = Vec3.ZERO;
        Shot(Vec3 axis, AttackType source, List<TrajectorySpec> specs) {
            this.axis = axis.normalize();
            bullet = BulletState.builder().position(Vec3.ZERO).velocity(this.axis).baseSpeed(1)
                    .attackType(source)
                    .range(100).lifetime(100000).trajectorySpecs(specs).build();
        }
        TrajectoryMotion step(double distance) {
            bullet.getTrajectoryRuntime().anchor(anchor);
            TrajectoryMotion m = TrajectoryEvaluator.evaluate(bullet, axis.scale(distance), distance, 1, MODULES::get);
            bullet.advanceTrajectory(m, 1);
            return m;
        }
    }

    private static Shot laser(Vec3 axis) {
        return new Shot(axis, TestAttackTypes.LASER, List.of(new TrajectorySpec(MyReflectionLaserTrajectoryModule.ID, 0)));
    }

    @Test void finalFloatingRangeResidueIsChargedEvenAfterPause() {
        Shot shot = laser(new Vec3(1, 0, 0));
        shot.step(MyReflectionLaserTrajectoryModule.PRELUDE);
        shot.bullet.restoreSnapshot(shot.bullet.position(), 0, Math.nextDown(100.0));
        shot.bullet.getTrajectoryRuntime().states().get(MyReflectionLaserTrajectoryModule.ID).path().suspend();
        shot.step(0.1);
        assertEquals(100, shot.bullet.traveled(), 0);
    }

    @Test void ordinaryRangeReplacesShortLifetimeAndClampsItsFinalStep() {
        var id = MyReflectionBulletTrajectoryModule.ID;
        BulletState bullet = BulletState.builder().velocity(new Vec3(1, 0, 0)).baseSpeed(1)
                .range(3.25).lifetime(2).trajectorySpecs(List.of(new TrajectorySpec(id, 0))).build();
        for (int i = 0; i < 4; i++) {
            TrajectoryMotion m = TrajectoryEvaluator.evaluate(bullet, new Vec3(1, 0, 0), 1, 1, MODULES::get);
            assertTrue(bullet.advanceTrajectory(m, 1));
        }
        assertEquals(3.25, bullet.traveled(), 1e-9);
        assertFalse(bullet.tickPhysics());
    }

    @Test void laserReturnsToOriginThenFollowsReverseThreeDimensionalAxis() {
        for (Vec3 axis : List.of(new Vec3(1, 0, 0), new Vec3(1, 1, 2), new Vec3(0, 1, 0))) {
            Shot shot = laser(axis);
            shot.step(MyReflectionLaserTrajectoryModule.PRELUDE);
            assertEquals(0, shot.bullet.position().length(), 1e-8);
            assertEquals(0, shot.bullet.traveled(), 1e-10);
            assertEquals(1, shot.bullet.velocity().normalize().dot(shot.axis.scale(-1)), 1e-8);
            shot.step(3);
            assertEquals(0, shot.bullet.position().subtract(shot.axis.scale(-3)).length(), 1e-8);
            assertEquals(3, shot.bullet.traveled(), 1e-8);
        }
    }

    @Test void laserUsesSymmetricSidesAndContinuousBoundaryTangents() {
        double radius = MyReflectionLaserTrajectoryModule.RADIUS;
        double arc = Math.PI * radius;
        for (double d : new double[]{2, 5, 5 + arc, 8 + arc, 10 + arc}) {
            Shot before = laser(new Vec3(1, 1, 0));
            Shot after = laser(new Vec3(1, 1, 0));
            before.step(d - 1e-4); after.step(d + 1e-4);
            assertTrue(before.bullet.position().distanceTo(after.bullet.position()) < 0.0003);
            assertTrue(before.bullet.velocity().scale(1 / (d - 1e-4))
                    .distanceTo(after.bullet.velocity().scale(1 / (d + 1e-4))) < 0.001);
        }
        Shot left = laser(new Vec3(1, 0, 0)); left.step(2);
        Shot right = laser(new Vec3(1, 0, 0)); right.step(8 + arc);
        assertEquals(2, left.bullet.position().x, 1e-8);
        assertEquals(2, right.bullet.position().x, 1e-8);
        assertEquals(-radius, left.bullet.position().z, 1e-8);
        assertEquals(radius, right.bullet.position().z, 1e-8);
    }

    @Test void planetPreludeCompletesBeforeReflectionRegardlessOfAttachmentOrder() {
        TrajectorySpec planet = new TrajectorySpec(TinyPlanetLaserTrajectoryModule.ID, 0);
        TrajectorySpec reflection = new TrajectorySpec(MyReflectionLaserTrajectoryModule.ID, 0);
        for (List<TrajectorySpec> specs : List.of(List.of(planet, reflection), List.of(reflection, planet))) {
            Shot shot = new Shot(new Vec3(1, 1, 0), TestAttackTypes.BRIMSTONE, specs);
            double end = new TinyPlanetLaserTrajectoryModule().maximumFreeDistance(0);
            for (int i = 0; i < 1000 && shot.bullet.traveled() == 0; i++) {
                shot.step(0.1);
                var planetState = shot.bullet.getTrajectoryRuntime().states().get(planet.id());
                var mirrorState = shot.bullet.getTrajectoryRuntime().states().get(reflection.id());
                if (planetState.path().distance() < end - 1e-8) {
                    assertTrue(mirrorState == null || mirrorState.path().distance() == 0);
                    assertEquals(0, shot.bullet.traveled(), 1e-12);
                }
            }
            assertTrue(shot.bullet.traveled() > 0, "combined laser must terminate its free prelude");
            assertEquals(0, shot.bullet.position().add(shot.axis.scale(shot.bullet.traveled())).length(), 1e-7);
            assertEquals(end, shot.bullet.getTrajectoryRuntime().states().get(planet.id()).path().distance(), 1e-8);
        }
    }

    @Test void homingPauseAndNetworkCopyPreservePhaseAndResumeFromCurrentPosition() {
        Shot server = laser(new Vec3(1, 0, 0));
        server.step(6);
        TrajectoryRuntimeState state = server.bullet.getTrajectoryRuntime().states().get(MyReflectionLaserTrajectoryModule.ID);
        server.bullet.setVelocity(new Vec3(0, 0.1, 0));
        server.bullet.tickPhysics();
        assertTrue(state.path().suspended());
        assertEquals(6, state.path().distance(), 1e-10);
        assertEquals(0.1, server.bullet.traveled(), 1e-10);
        Shot client = laser(new Vec3(1, 0, 0));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            state.copy().write(buffer);
            client.bullet.getTrajectoryRuntime().states().put(MyReflectionLaserTrajectoryModule.ID, TrajectoryRuntimeState.read(buffer));
        } finally { buffer.release(); }
        client.bullet.setPosition(server.bullet.position());
        client.bullet.setVelocity(server.bullet.velocity());
        client.bullet.restoreSnapshot(server.bullet.position(), server.bullet.age(), server.bullet.traveled());
        Vec3 current = server.bullet.position();
        server.step(1e-5); client.step(1e-5);
        assertTrue(server.bullet.position().distanceTo(current) < 0.00002);
        for (int i = 0; i < 200; i++) {
            server.step(0.1); client.step(0.1);
            assertEquals(server.bullet.position(), client.bullet.position());
        }
        assertTrue(server.bullet.traveled() > 0.1);
        assertNotSame(state, client.bullet.getTrajectoryRuntime().states().get(MyReflectionLaserTrajectoryModule.ID));
    }

    @Test void ordinaryCanTurnFromExactlyOppositeAndKeepsItsSpawnTarget() {
        for (AttackType source : List.of(TestAttackTypes.BULLET, TestAttackTypes.C_SECTION)) {
            Shot shot = new Shot(new Vec3(1, 0, 0), source,
                    List.of(new TrajectorySpec(MyReflectionBulletTrajectoryModule.ID, 0)));
            shot.bullet.setPosition(new Vec3(5, 0, 0));
            boolean slowed = false;
            boolean returned = false;
            for (int i = 0; i < 500; i++) {
                if (i == 100) shot.anchor = new Vec3(10, 2, -10);
                Vec3 oldPosition = shot.bullet.position();
                TrajectoryMotion m = shot.step(0.2);
                assertTrue(shot.bullet.velocity().length() <= 0.20000001);
                slowed |= shot.bullet.velocity().length() < 0.08;
                returned |= shot.bullet.velocity().x < -0.05;
                assertTrue(oldPosition.distanceTo(shot.bullet.position()) <= 0.20000001);
                assertTrue(m.chargedDistance(0) <= 0.20000001);
            }
            assertTrue(slowed, "The outward motion must slow before returning");
            assertTrue(returned, "The force must reverse the outgoing velocity");
            assertEquals(0, shot.bullet.position().z, 1e-10);
        }
    }
    @Test void ordinaryTurnsTowardSpawnWithoutAccelerating() {
        BulletState bullet = BulletState.builder().position(new Vec3(5, 0, 0))
                .restoreTrajectory(new TrajectoryRuntime(new Vec3(5, 0, 25), new Vec3(1, 0, 0), List.of()).snapshot())
                .velocity(new Vec3(1, 0, 0)).baseSpeed(1).range(100).build();
        TrajectoryMotion motion = new MyReflectionBulletTrajectoryModule().apply(new TrajectoryContext(bullet, bullet.position(), bullet.velocity(), bullet.velocity(), Vec3.ZERO, 0, 0, 1, new Vec3(5, 0, 10), 0, new TrajectoryRuntimeState()));
        assertTrue(motion.desiredVelocity().length() <= 1.00000001);
        assertTrue(motion.desiredVelocity().z > 0.1);
        assertTrue(motion.desiredPosition().z > 0);
    }

    @Test void laserPreludeDoesNotConsumeRange() {
        BulletState bullet = BulletState.builder().position(Vec3.ZERO).velocity(new Vec3(1, 0, 0))
                .baseSpeed(1).range(100).attackType(TestAttackTypes.LASER).build();
        TrajectoryMotion motion = new MyReflectionLaserTrajectoryModule().apply(new TrajectoryContext(bullet, Vec3.ZERO, bullet.velocity(), new Vec3(0.1, 0, 0), Vec3.ZERO, 0, 0, 1, Vec3.ZERO, 0, new TrajectoryRuntimeState()));
        assertEquals(0, motion.chargedDistance(0.1), 1e-12);
    }

    @Test void ownerMovementCannotChangeReflectionAndTurningStaysHorizontal() {
        Shot stationary = new Shot(new Vec3(1, 0, 0), TestAttackTypes.BULLET,
                List.of(new TrajectorySpec(MyReflectionBulletTrajectoryModule.ID, 0)));
        Shot moving = new Shot(new Vec3(1, 0, 0), TestAttackTypes.BULLET,
                List.of(new TrajectorySpec(MyReflectionBulletTrajectoryModule.ID, 0)));
        for (int i = 0; i < 100; i++) {
            moving.anchor = new Vec3(i * 3, -20, i);
            stationary.step(0.2); moving.step(0.2);
            assertEquals(stationary.bullet.position(), moving.bullet.position());
            assertEquals(0, moving.bullet.position().y, 1e-10);
        }
    }

    @Test void horizontalTurnTakesPriorityEvenWhenOriginIsBelowProjectile() {
        Shot shot = new Shot(new Vec3(1, 0, 0), TestAttackTypes.BULLET,
                List.of(new TrajectorySpec(MyReflectionBulletTrajectoryModule.ID, 0)));
        shot.bullet.setPosition(new Vec3(20, 2, 0));
        shot.step(1);
        assertTrue(shot.bullet.position().y > 1.99);
        assertEquals(0, shot.bullet.position().z, 1e-10);
        assertTrue(Math.abs(shot.bullet.velocity().y) < 0.02);
        assertTrue(shot.bullet.velocity().x < 0.9);
    }

    @Test void ordinaryBuilderUsesFinalSpawnAndSnapshotCanRestoreOriginalSpawn() {
        Vec3 spawn = new Vec3(20, 60, -4);
        BulletState bullet = BulletState.builder().position(spawn).build();
        assertEquals(spawn, bullet.getTrajectoryRuntime().origin());
        bullet.setPosition(spawn.add(5, 0, 0));
        assertEquals(spawn, bullet.getTrajectoryRuntime().origin());
        BulletState restored = BulletState.builder().position(bullet.position()).restoreTrajectory(bullet.getTrajectoryRuntime().snapshot()).build();
        assertEquals(spawn, restored.getTrajectoryRuntime().origin());
    }

    @Test void attractionIsWeakNearbyAndStrengthensSmoothlyBeyondTwelveBlocks() {
        assertEquals(0, MyReflectionBulletTrajectoryModule.pullAtDistance(0), 0);
        assertTrue(MyReflectionBulletTrajectoryModule.pullAtDistance(6) < 0.01);
        assertEquals(0.018, MyReflectionBulletTrajectoryModule.pullAtDistance(12), 1e-10);
        assertEquals(0.18, MyReflectionBulletTrajectoryModule.pullAtDistance(18), 1e-10);
        double previous = 0;
        for (double d = 0; d <= 30; d += 0.01) {
            double rate = MyReflectionBulletTrajectoryModule.pullAtDistance(d);
            assertTrue(rate >= previous && rate - previous < 0.001);
            previous = rate;
        }
    }
}
