package net.luojiuoscar.isaac_disaster.bullet;

import static org.junit.jupiter.api.Assertions.*;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.TinyPlanetBulletTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.TinyPlanetLaserTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.TinyPlanetTrajectoryModule;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class TinyPlanetTrajectoryTest {
    @Test
    void registeredModuleClassSelectsBulletOrLaserBehavior() {
        BulletState laserVariant =
            BulletState.builder()
                .attackType(TestAttackTypes.LASER)
                .position(Vec3.ZERO)
                .velocity(new Vec3(1, 0, 0))
                .range(200)
                .baseSpeed(1)
                .build();
        BulletState tearVariant =
            BulletState.builder()
                .attackType(TestAttackTypes.BULLET)
                .position(Vec3.ZERO)
                .velocity(new Vec3(1, 0, 0))
                .range(200)
                .baseSpeed(1)
                .build();

        var bulletMotion =
            new TinyPlanetBulletTrajectoryModule()
                .apply(
                    new TrajectoryContext(
                        laserVariant,
                        Vec3.ZERO,
                        laserVariant.velocity(),
                        new Vec3(4, 0, 0),
                        Vec3.ZERO,
                        0,
                        0,
                        1,
                        Vec3.ZERO,
                        0,
                        new TrajectoryRuntimeState()));
        var laserMotion =
            new TinyPlanetLaserTrajectoryModule()
                .apply(
                    new TrajectoryContext(
                        tearVariant,
                        Vec3.ZERO,
                        tearVariant.velocity(),
                        new Vec3(4, 0, 0),
                        Vec3.ZERO,
                        0,
                        0,
                        1,
                        Vec3.ZERO,
                        0,
                        new TrajectoryRuntimeState()));

        assertEquals(4, bulletMotion.rangeCost(), 1e-8);
        assertEquals(0, laserMotion.rangeCost(), 1e-8);
    }

    @Test
    void laserConsumesTheLastRepresentableRangeAfterHoming() {
        BulletState bullet =
            BulletState.builder()
                .position(Vec3.ZERO)
                .velocity(new Vec3(1, 0, 0))
                .range(16)
                .lifetime(1000)
                .attackType(TestAttackTypes.LASER)
                .build();
        TrajectoryRuntimeState state = new TrajectoryRuntimeState();
        TinyPlanetTrajectoryModule module = new TinyPlanetLaserTrajectoryModule();
        var first =
            module.apply(
                new TrajectoryContext(
                    bullet,
                    Vec3.ZERO,
                    bullet.velocity(),
                    new Vec3(39, 0, 0),
                    Vec3.ZERO,
                    0,
                    0,
                    1,
                    Vec3.ZERO,
                    0,
                    state));
        bullet.setPosition(first.desiredPosition());
        bullet.restoreSnapshot(bullet.position(), 0, Math.nextDown(16.0));
        state.path().suspend();
        var last =
            module.apply(
                new TrajectoryContext(
                    bullet,
                    bullet.position(),
                    bullet.velocity(),
                    new Vec3(0.1, 0, 0),
                    Vec3.ZERO,
                    state.path().distance(),
                    0,
                    1,
                    Vec3.ZERO,
                    0,
                    state));
        assertEquals(
            16.0,
            bullet.traveled() + last.rangeCost(),
            "Final rounding residue must not stall the laser loop");
    }

    @Test
    void ordinaryFamiliesUsePitchHeightClampedAtSixtyDegrees() {
        for (var source : java.util.List.of(TestAttackTypes.BULLET, TestAttackTypes.C_SECTION)) {
            for (double degrees : new double[] {-90, -60, -30, 0, 30, 60, 90}) {
                double radians = Math.toRadians(degrees);
                Simulation sim =
                    new Simulation(source, new Vec3(Math.cos(radians), Math.sin(radians), 0), 0);
                sim.step(12);
                double ratio = Math.max(0.1, Math.min(0.9, 0.5 + degrees / 60 * 0.4));
                assertEquals(
                    (ratio - 0.6) * 1.8, sim.bullet.position().y, 1e-7, source + " pitch=" + degrees);
            }
        }
    }

    @Test
    void launchSegmentFollowsViewForEveryProjectileFamily() {
        for (var source : TestAttackTypes.PROJECTILES) {
            for (Vec3 axis :
                java.util.List.of(
                    new Vec3(1, 0, 0), new Vec3(0, 0, -1), new Vec3(1, 1, 1), new Vec3(0, 1, 0))) {
                Simulation sim = new Simulation(source, axis, 0);
                var motion = sim.step(0.5);
                assertEquals(0, motion.desiredPosition().distanceTo(axis.normalize().scale(0.5)), 1e-8);
            }
        }
    }

    @Test
    void movingPlayerCannotTeleportOrAccelerateOrdinaryBullets() {
        for (var source : java.util.List.of(TestAttackTypes.BULLET, TestAttackTypes.C_SECTION)) {
            Simulation sim = new Simulation(source, new Vec3(1, 0, 0), 0);
            sim.step(8);
            for (int i = 0; i < 30; i++) {
                sim.anchor = sim.anchor.add(2, 1, -1);
                Vec3 before = sim.bullet.position();
                var motion = sim.step(0.2);
                assertTrue(before.distanceTo(motion.desiredPosition()) <= 0.2 + 1e-8);
                assertTrue(motion.desiredVelocity().length() <= 0.2 + 1e-8);
                Vec3 previous = before;
                double pathLength = 0;
                for (Vec3 point : motion.path()) {
                    pathLength += point.distanceTo(previous);
                    previous = point;
                }
                assertTrue(pathLength <= 0.2 + 1e-8);
            }
            assertTrue(
                sim.bullet.position().distanceTo(sim.anchor) > 20,
                "Faster player can escape the projectile");
        }
    }

    private static final class Simulation {
        final BulletState bullet;
        final TrajectoryRuntimeState state = new TrajectoryRuntimeState();
        final TrajectoryModule module;
        final Vec3 axis;
        final int amplifier;
        Vec3 anchor = Vec3.ZERO;

        Simulation(AttackType source, Vec3 axis, int amplifier) {
            this.axis = axis.normalize();
            this.amplifier = amplifier;
            boolean laser = source == TestAttackTypes.LASER || source == TestAttackTypes.BRIMSTONE;
            module =
                laser ? new TinyPlanetLaserTrajectoryModule() : new TinyPlanetBulletTrajectoryModule();
            var id =
                laser
                    ? ModTrajectoryModules.TINY_PLANET_LASER.getId()
                    : ModTrajectoryModules.TINY_PLANET_BULLET.getId();
            bullet =
                BulletState.builder()
                    .attackType(source)
                    .position(Vec3.ZERO)
                    .velocity(this.axis)
                    .trajectorySpecs(java.util.List.of(new TrajectorySpec(id, amplifier)))
                    .range(200)
                    .lifetime(laser ? 100000 : 2)
                    .baseSpeed(1)
                    .build();
        }

        TrajectoryMotion step(double length) {
            var result =
                module.apply(
                    new TrajectoryContext(
                        bullet,
                        bullet.position(),
                        bullet.velocity(),
                        axis.scale(length),
                        Vec3.ZERO,
                        state.path().distance(),
                        0,
                        1,
                        anchor,
                        amplifier,
                        state));
            bullet.advanceTrajectory(result, 1);
            return result;
        }
    }

    @Test
    void laserOrbitUsesInclinedPlaneContainingLaunchAxis() {
        for (var source : java.util.List.of(TestAttackTypes.LASER, TestAttackTypes.BRIMSTONE)) {
            Vec3 axis = new Vec3(1, 1, 0).normalize();
            Simulation sim = new Simulation(source, axis, 0);
            sim.step(3);
            Vec3 center = sim.bullet.position().subtract(axis.scale(3));
            Vec3 normal = new Vec3(0, 1, 0).subtract(axis.scale(axis.y)).normalize();
            double initialHeight = sim.bullet.position().y;
            for (int i = 0; i < 100; i++) {
                sim.step(6 * Math.PI / 100);
                Vec3 radial = sim.bullet.position().subtract(center);
                assertEquals(0, radial.dot(normal), 1e-7);
                assertEquals(3, radial.length(), 1e-7);
                if (i == 49) assertTrue(Math.abs(sim.bullet.position().y - initialHeight) > 4);
            }
        }
    }

    @Test
    void lasersFinishOneCircleThenRestoreThePitchedLaunchLineWithFullRange() {
        for (var source : java.util.List.of(TestAttackTypes.LASER, TestAttackTypes.BRIMSTONE)) {
            for (Vec3 axis : java.util.List.of(new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(0, 1, 0))) {
                Simulation sim = new Simulation(source, axis, 1);
                double freeEnd = 5 + 8 * Math.PI;
                sim.step(3);
                Vec3 entry = sim.bullet.position();
                sim.step(8 * Math.PI);
                assertEquals(0, entry.distanceTo(sim.bullet.position()), 1e-7);
                assertEquals(0, sim.bullet.traveled(), 1e-7);
                sim.step(2.25);
                assertEquals(freeEnd + 0.25, sim.state.path().distance(), 1e-7);
                assertEquals(0.25, sim.bullet.traveled(), 1e-7);
                assertEquals(1, sim.bullet.velocity().normalize().dot(axis.normalize()), 1e-6);
                assertEquals(0, sim.bullet.position().cross(axis).length(), 1e-7);
            }
        }
    }

    @Test
    void ordinaryHeightAndRadiusFollowAnchorWithoutResamplingHeading() {
        for (var source : java.util.List.of(TestAttackTypes.BULLET, TestAttackTypes.C_SECTION)) {
            for (Vec3 axis :
                java.util.List.of(new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(0, -1, 0))) {
                Simulation sim = new Simulation(source, axis, 2);
                sim.step(10);
                Vec3 old = sim.bullet.position();
                sim.anchor = new Vec3(3, 4, 5);
                sim.bullet.setVelocity(new Vec3(0, 0, 1));
                sim.step(0);
                assertEquals(
                    old, sim.bullet.position(), "Moving the player alone cannot move the projectile");
                for (int i = 0; i < 300; i++) sim.step(0.1);
                assertEquals(5, Math.hypot(sim.bullet.position().x - 3, sim.bullet.position().z - 5), 1e-7);
                double pitch = Math.toDegrees(Math.asin(axis.normalize().y));
                double ratio = Math.max(0.1, Math.min(0.9, 0.5 + 0.4 * pitch / 60));
                assertEquals(4 + (ratio - 0.6) * 1.8, sim.bullet.position().y, 1e-7);
            }
        }
    }

    @Test
    void entryAndExitHaveContinuousTangents() {
        double eps = 1e-5;
        for (double boundary : new double[] {1, 3, 3 + 6 * Math.PI, 5 + 6 * Math.PI}) {
            Simulation sim = new Simulation(TestAttackTypes.LASER, new Vec3(1, 1, 1), 0);
            var left = sim.step(boundary - eps);
            var right = sim.step(2 * eps);
            Vec3 leftDirection = left.desiredVelocity().scale(1 / left.desiredVelocity().length());
            Vec3 rightDirection = right.desiredVelocity().scale(1 / right.desiredVelocity().length());
            assertTrue(
                leftDirection.distanceTo(rightDirection) < 0.002, "Tangent at distance " + boundary);
            assertTrue(left.desiredPosition().distanceTo(right.desiredPosition()) < 0.001);
        }
    }

    @Test
    void suspensionPreservesPhaseAndResumesFromCurrentPosition() {
        Simulation sim = new Simulation(TestAttackTypes.BULLET, new Vec3(1, 0, 0), 0);
        sim.step(4);
        double phase = sim.state.path().distance();
        sim.state.path().suspend();
        sim.bullet.setPosition(new Vec3(9, 8, 7));
        sim.bullet.setVelocity(new Vec3(1, 0, 0));
        var resumed = sim.step(0);
        assertEquals(phase, sim.state.path().distance());
        assertEquals(new Vec3(9, 8, 7), resumed.desiredPosition());
        sim.step(2);
        assertTrue(sim.bullet.position().distanceTo(new Vec3(9, 8, 7)) <= 2 + 1e-7);
        for (int i = 0; i < 300; i++) sim.step(0.1);
        assertEquals(3, Math.hypot(sim.bullet.position().x, sim.bullet.position().z), 1e-7);
    }

    @Test
    void pathMemoryRoundTripsAndCopiesIndependently() {
        Simulation sim = new Simulation(TestAttackTypes.BULLET, new Vec3(1, 1, 0), 0);
        sim.step(5);
        var buf = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            sim.state.write(buf);
            var decoded = TrajectoryRuntimeState.read(buf);
            var copy = decoded.copy();
            copy.path().suspend();
            assertFalse(decoded.path().suspended());
            assertEquals(5, decoded.path().distance());
            assertEquals(sim.state.phase(), decoded.phase());
        } finally {
            buf.release();
        }
    }

    @Test
    void rangeBudgetClampsFinalStepAndCollisionTruncatesOnlyItsShare() {
        Simulation sim = new Simulation(TestAttackTypes.BULLET, new Vec3(1, 0, 0), 0);
        sim.step(199.75);
        var last = sim.step(2);
        assertEquals(0.25, last.rangeCost(), 1e-7);
        assertEquals(200, sim.bullet.traveled(), 1e-7);
        sim.bullet.retainTrajectoryFraction(0.4);
        assertEquals(199.85, sim.bullet.traveled(), 1e-7);
    }

    @Test
    void ordinaryOrbitKeepsFullRadiusForMultipleRevolutions() {
        BulletState bullet =
            BulletState.builder()
                .position(Vec3.ZERO)
                .velocity(new Vec3(1, 0, 0))
                .baseSpeed(1)
                .range(200)
                .build();
        TrajectoryRuntimeState state = new TrajectoryRuntimeState();
        TinyPlanetTrajectoryModule module = new TinyPlanetBulletTrajectoryModule();
        for (int i = 0; i < 1000; i++) {
            var motion =
                module.apply(
                    new TrajectoryContext(
                        bullet,
                        bullet.position(),
                        bullet.velocity(),
                        new Vec3(0.1, 0, 0),
                        Vec3.ZERO,
                        i * 0.1,
                        i,
                        1,
                        Vec3.ZERO,
                        0,
                        state));
            Vec3 next = motion.desiredPosition();
            if (i > 60) assertEquals(3, Math.hypot(next.x, next.z), 1e-7);
            bullet.setPosition(next);
            bullet.setVelocity(motion.desiredVelocity());
        }
    }
}
