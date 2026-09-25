package net.luojiuoscar.isaac_disaster.bullet;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import net.luojiuoscar.isaac_disaster.bullet.core.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class TrajectorySamplingTest {
    @Test
    void unknownPrimaryModuleKeepsBaseMotion() {
        var bullet = bullet(TestAttackTypes.BULLET, new Vec3(1, 0, 0), "unknown");
        var motion = TrajectoryEvaluator.evaluate(bullet, new Vec3(1, 0, 0), 1, 1, MODULES::get);
        assertEquals(new Vec3(1, 0, 0), motion.desiredPosition());
    }

    @Test
    void laserBudgetCountsEachRemainingPreludeOnceAndHookDoesNotPausePrimary() {
        var b =
            bullet(
                TestAttackTypes.LASER,
                new Vec3(1, 0, 0),
                "tiny_planet_laser",
                "my_reflection_laser",
                "hook_worm");
        double expected =
            MODULES.get(ModTrajectoryModules.TINY_PLANET_LASER.getId()).maximumFreeDistance(0)
                + MyReflectionLaserTrajectoryModule.PRELUDE
                + 5;
        assertEquals(expected, TrajectoryEvaluator.remainingFreeDistance(b, MODULES::get), 1e-8);
        step(b, 1);
        assertEquals(expected - 1, TrajectoryEvaluator.remainingFreeDistance(b, MODULES::get), 1e-8);
        var hook = bullet(TestAttackTypes.LASER, new Vec3(1, 0, 0), "hook_worm");
        assertEquals(42, TrajectoryEvaluator.remainingFreeDistance(hook, MODULES::get));
    }

    @Test
    void everyWormPairPreservesPrimaryReachAndAttachmentOrder() {
        var names = List.of("hook_worm", "wiggle_worm", "ring_worm", "ouroboros_worm");
        for (var variant : TestAttackTypes.PROJECTILES)
            for (int i = 0; i < names.size(); i++)
            for (int j = i + 1; j < names.size(); j++) {
                var a = bullet(variant, new Vec3(1, 1, 1), names.get(i), names.get(j));
                var b = bullet(variant, new Vec3(1, 1, 1), names.get(j), names.get(i));
                // Hook needs 40 forward + 37 lateral blocks of motion for this range.
                for (int tick = 0; tick < 80 && a.traveled() < 40 - 1e-8; tick++) {
                    step(a, 1);
                    step(b, 1);
                    assertTrue(a.position().distanceTo(b.position()) < 1e-7);
                    assertEquals(a.traveled(), frame(a).position().length(), 1e-7);
                }
                assertEquals(40, a.traveled(), 1e-7);
            }
    }

    @Test
    void entryIsSmoothAndAdditionalStacksIncreaseOnlyAmplitude() {
        for (var module :
            List.of(
                new WiggleWormTrajectoryModule(),
                new RingWormTrajectoryModule(),
                new OuroborosWormTrajectoryModule())) {
            var f = new TrajectoryKinematics();
            f.initializeLaunchBasis(new Vec3(1, 1, 1));
            f.orient(new Vec3(1, 1, 1));
            var s = newOffsetState(module);
            assertEquals(0, module.offset(f, s, 0).length(), 0);
            setPhase(s, 0.001);
            assertTrue(module.offset(f, s, 2).length() < 1e-7);
            setPhase(s, module instanceof WiggleWormTrajectoryModule ? 3 : 2);
            assertEquals(module.amplitude(2), module.offset(f, s, 2).length(), 1e-8);
            if (module instanceof OuroborosWormTrajectoryModule)
                assertEquals(0, module.offset(f, s, 2).dot(f.right()), 1e-8);
        }
    }

    @Test
    void wormBasisRemainsFixedWhenPrimaryDirectionChangesPitch() {
        var f = new TrajectoryKinematics();
        f.initializeLaunchBasis(new Vec3(1, 0, 0));
        Vec3 side = f.launchRight();
        Vec3 up = f.launchUp();
        f.orient(new Vec3(1, 1, 0));
        assertTrue(f.launchRight().distanceTo(side) < 1e-12);
        assertTrue(f.launchUp().distanceTo(up) < 1e-12);
        f.orient(new Vec3(1, -1, 0));
        assertTrue(f.launchRight().distanceTo(side) < 1e-12);
        assertTrue(f.launchUp().distanceTo(up) < 1e-12);
    }

    @Test
    void laserPreludesStayFreeWhileWormPhaseAdvancesAndResumeIsFinite() {
        for (String primary : List.of("tiny_planet_laser", "my_reflection_laser")) {
            var b = bullet(TestAttackTypes.LASER, new Vec3(1, 1, 0), primary, "ring_worm", "hook_worm");
            for (int i = 0; i < 10; i++) step(b, 0.1);
            assertEquals(0, b.traveled(), 1e-8);
            assertTrue(phase(b.getTrajectoryRuntime().states().get(id("ring_worm"))) > 0.1);
            for (int repeat = 0; repeat < 3; repeat++) {
                double phase = phase(b.getTrajectoryRuntime().states().get(id("ring_worm")));
                b.setVelocity(new Vec3(0, 0, 0.2));
                b.tickPhysics();
                Vec3 start = b.position();
                step(b, 0.01);
                assertTrue(b.position().distanceTo(start) < 0.1);
                assertTrue(phase(b.getTrajectoryRuntime().states().get(id("ring_worm"))) >= phase);
            }
            int ticks = 0;
            while (b.traveled() < 40 - 1e-7 && ticks++ < 1000) step(b, 0.1);
            assertTrue(ticks < 1000);
        }
    }

    @Test
    void mirrorReversalDoesNotTeleportLongitudinalOffsets() {
        var b =
            bullet(TestAttackTypes.BULLET, new Vec3(1, 0, 0), "my_reflection_bullet", "ouroboros_worm");
        boolean returned = false;
        for (int i = 0; i < 400; i++) {
            Vec3 before = b.position();
            step(b, 0.1);
            assertTrue(before.distanceTo(b.position()) < 0.5);
            returned |= frame(b).velocity().x < 0;
        }
        assertTrue(returned);
    }

    static ResourceLocation id(String name) {
        return ResourceLocation.parse("isaac_disaster:" + name);
    }

    private static TrajectoryState newOffsetState(OffsetTrajectoryModule module) {
        if (module instanceof WiggleWormTrajectoryModule) return new WiggleWormTrajectoryModule.State();
        if (module instanceof RingWormTrajectoryModule) return new RingWormTrajectoryModule.State();
        return new OuroborosWormTrajectoryModule.State();
    }

    private static double phase(TrajectoryState state) {
        if (state instanceof WiggleWormTrajectoryModule.State value) return value.phase();
        if (state instanceof RingWormTrajectoryModule.State value) return value.phase();
        if (state instanceof OuroborosWormTrajectoryModule.State value) return value.phase();
        return 0;
    }

    private static void setPhase(TrajectoryState state, double value) {
        if (state instanceof WiggleWormTrajectoryModule.State target) target.phase(value);
        else if (state instanceof RingWormTrajectoryModule.State target) target.phase(value);
        else if (state instanceof OuroborosWormTrajectoryModule.State target) target.phase(value);
    }

    static final Map<ResourceLocation, TrajectoryModule> MODULES =
        Map.of(
            id("hook_worm"),
            new HookWormTrajectoryModule(),
            id("wiggle_worm"),
            new WiggleWormTrajectoryModule(),
            id("ring_worm"),
            new RingWormTrajectoryModule(),
            id("ouroboros_worm"),
            new OuroborosWormTrajectoryModule(),
            ModTrajectoryModules.GRAVITY.getId(),
            new GravityTrajectoryModule(),
            ModTrajectoryModules.MY_REFLECTION_BULLET.getId(),
            new MyReflectionBulletTrajectoryModule(),
            ModTrajectoryModules.TINY_PLANET_BULLET.getId(),
            new TinyPlanetBulletTrajectoryModule(),
            ModTrajectoryModules.TINY_PLANET_LASER.getId(),
            new TinyPlanetLaserTrajectoryModule(),
            ModTrajectoryModules.MY_REFLECTION_LASER.getId(),
            new MyReflectionLaserTrajectoryModule());

    static BulletState bullet(AttackType variant, Vec3 axis, String... modules) {
        return BulletState.builder()
            .attackType(variant)
            .position(Vec3.ZERO)
            .velocity(axis.normalize())
            .baseSpeed(1)
            .range(40)
            .lifetime(2)
            .trajectorySpecs(Arrays.stream(modules).map(n -> new TrajectorySpec(id(n), 0)).toList())
            .build();
    }

    static TrajectoryMotion step(BulletState b, double dt) {
        var motion =
            TrajectoryEvaluator.evaluate(
                b,
                b.getTrajectoryRuntime().launchDirection().scale(b.baseSpeed()),
                1,
                dt,
                MODULES::get);
        b.advanceTrajectory(motion, dt);
        return motion;
    }

    static TrajectoryKinematics frame(BulletState b) {
        return b.getTrajectoryRuntime().kinematics();
    }

    @Test
    void hookHasExplicitCornersAndFreeLateralMotionForAllVariants() {
        for (var variant : TestAttackTypes.PROJECTILES) {
            var b = bullet(variant, new Vec3(1, 0, 0), "hook_worm");
            step(b, 2);
            assertEquals(2, b.position().x, 1e-8);
            var lateral = step(b, 1);
            assertTrue(b.position().distanceTo(new Vec3(2, 0, 1)) < 1e-8);
            assertEquals(0, lateral.rangeCost(), 1e-8);
            step(b, 2);
            assertEquals(4, b.position().x, 1e-8);
            step(b, 2);
            assertEquals(-1, b.position().z, 1e-8);
            assertEquals(4, b.traveled(), 1e-8);
            assertTrue(b.isAlive());
        }
    }

    @Test
    void ringContinuesMultipleOrbitsInPerpendicularPlane() {
        for (var variant : TestAttackTypes.PROJECTILES)
            for (Vec3 axis : List.of(new Vec3(1, 0, 0), new Vec3(1, 1, 1), new Vec3(0, 1, 0))) {
                var b = bullet(variant, axis, "ring_worm");
                for (int i = 0; i < 24; i++) {
                    step(b, 1);
                    var k = frame(b);
                    assertEquals(0, k.offset().dot(axis.normalize()), 1e-8);
                    if (i >= 1) assertEquals(1, k.offset().length(), 1e-8);
                    assertEquals(i + 1, b.traveled(), 1e-8);
                    assertEquals(i + 1, k.position().dot(axis.normalize()), 1e-8);
                }
            }
    }

    @Test
    void wiggleIsCenteredAndOuroborosUsesVerticalPlaneForBothRoots() {
        for (var variant : TestAttackTypes.PROJECTILES) {
            var b = bullet(variant, new Vec3(1, 0, 0), "wiggle_worm");
            step(b, 3);
            assertEquals(-1, b.position().z, 1e-8);
            step(b, 1);
            assertEquals(0, b.position().z, 1e-8);
            var o = bullet(variant, new Vec3(1, 0, 0), "ouroboros_worm");
            step(o, 2);
            assertEquals(1, o.position().y, 1e-8);
            assertEquals(0, o.position().z, 1e-8);
            step(o, 2);
            assertEquals(3, o.position().x, 1e-8);
            assertEquals(4, o.traveled(), 1e-8);
        }
    }

    @Test
    void hookPausesOtherWormPhasesOnlyWhenPrimaryIsStationary() {
        var b = bullet(TestAttackTypes.BULLET, new Vec3(1, 0, 0), "hook_worm", "ring_worm");
        step(b, 2);
        double phase = phase(b.getTrajectoryRuntime().states().get(id("ring_worm")));
        step(b, 1);
        assertEquals(phase, phase(b.getTrajectoryRuntime().states().get(id("ring_worm"))), 1e-8);
        var g = bullet(TestAttackTypes.BULLET, new Vec3(1, 0, 0), "hook_worm", "ring_worm", "gravity");
        step(g, 2);
        double distance = frame(g).distance();
        step(g, 1);
        assertTrue(frame(g).distance() > distance);
    }

    @Test
    void offsetsNeverFeedBackIntoGravityOrReflectionAndOrderDoesNotMatter() {
        for (String primary : List.of("gravity", "my_reflection_bullet", "tiny_planet_bullet")) {
            var plain = bullet(TestAttackTypes.BULLET, new Vec3(1, 0, 0), primary);
            var a =
                bullet(
                    TestAttackTypes.BULLET,
                    new Vec3(1, 0, 0),
                    primary,
                    "ring_worm",
                    "wiggle_worm",
                    "ouroboros_worm");
            var b =
                bullet(
                    TestAttackTypes.BULLET,
                    new Vec3(1, 0, 0),
                    "ouroboros_worm",
                    "wiggle_worm",
                    "ring_worm",
                    primary);
            for (int i = 0; i < 15; i++) {
                step(plain, 1);
                step(a, 1);
                step(b, 1);
                assertTrue(frame(a).position().distanceTo(plain.position()) < 1e-7, primary);
                assertTrue(frame(a).velocity().distanceTo(plain.velocity()) < 1e-7, primary);
                assertTrue(a.position().distanceTo(b.position()) < 1e-7);
                assertEquals(plain.traveled(), a.traveled(), 1e-7);
            }
        }
    }

    @Test
    void sampledPathsIncludeCurvesCornersAndExactRangeCosts() {
        var b = bullet(TestAttackTypes.LASER, new Vec3(1, 0, 0), "hook_worm");
        var m = step(b, 7);
        assertTrue(m.path().stream().anyMatch(v -> v.distanceTo(new Vec3(2, 0, 0)) < 1e-8));
        assertTrue(m.path().stream().anyMatch(v -> v.distanceTo(new Vec3(2, 0, 1)) < 1e-8));
        assertEquals(4, m.pathCosts().stream().mapToDouble(Double::doubleValue).sum(), 1e-8);
        assertTrue(m.pathCosts().contains(0.0));
        var ring = bullet(TestAttackTypes.BULLET, new Vec3(1, 0, 0), "ring_worm");
        assertTrue(step(ring, 8).path().stream().anyMatch(v -> v.y < -0.99));
    }

    @Test
    void stateCopiesAndNetworkPreservePhaseAndPrimaryVelocity() {
        var b = bullet(TestAttackTypes.C_SECTION, new Vec3(1, 1, 0), "hook_worm", "ring_worm");
        step(b, 2.5);
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        var copy = bullet(TestAttackTypes.C_SECTION, new Vec3(1, 1, 0), "hook_worm", "ring_worm");
        try {
            b.getTrajectoryRuntime().snapshot().write(buffer);
            copy.getTrajectoryRuntime().restore(TrajectoryRuntime.Snapshot.read(buffer));
        } finally {
            buffer.release();
        }
        copy.setPosition(b.position());
        copy.setVelocity(b.velocity());
        for (int i = 0; i < 5; i++) {
            step(b, 0.2);
            step(copy, 0.2);
            assertTrue(b.position().distanceTo(copy.position()) < 1e-8);
        }
        assertNotSame(frame(b), frame(copy));
    }

    @Test
    void trackingResumesFromCurrentPointWithoutResettingPhase() {
        var b = bullet(TestAttackTypes.BULLET, new Vec3(1, 0, 0), "ring_worm");
        step(b, 5);
        double phase = phase(b.getTrajectoryRuntime().states().get(id("ring_worm")));
        b.setVelocity(new Vec3(0, 1, 0));
        b.tickPhysics();
        b.setPosition(new Vec3(30, 20, 10));
        Vec3 before = b.position();
        step(b, 0.01);
        assertTrue(b.position().distanceTo(before) < 0.02);
        assertTrue(phase(b.getTrajectoryRuntime().states().get(id("ring_worm"))) > phase);
        assertTrue(frame(b).forward().y > 0.99);
    }
}
