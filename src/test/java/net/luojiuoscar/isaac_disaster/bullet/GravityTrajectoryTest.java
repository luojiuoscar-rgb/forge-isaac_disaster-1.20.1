package net.luojiuoscar.isaac_disaster.bullet;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import net.luojiuoscar.isaac_disaster.bullet.core.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.GravityTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.MyReflectionBulletTrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.MyReflectionLaserTrajectoryModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GravityTrajectoryTest {
    private static final Map<ResourceLocation, TrajectoryModule> MODULES =
        Map.of(
            ModTrajectoryModules.GRAVITY.getId(), new GravityTrajectoryModule(),
            ModTrajectoryModules.MY_REFLECTION_BULLET.getId(),
                new MyReflectionBulletTrajectoryModule(),
            ModTrajectoryModules.MY_REFLECTION_LASER.getId(),
                new MyReflectionLaserTrajectoryModule());

    private static BulletState bullet(AttackType source, ResourceLocation... ids) {
        return BulletState.builder()
            .position(Vec3.ZERO)
            .velocity(new Vec3(1, 0, 0))
            .baseSpeed(1)
            .range(40)
            .lifetime(source == TestAttackTypes.LASER || source == TestAttackTypes.BRIMSTONE ? 1000 : 2)
            .attackType(source)
            .trajectorySpecs(Arrays.stream(ids).map(id -> new TrajectorySpec(id, 0)).toList())
            .build();
    }

    private static TrajectoryMotion step(BulletState b) {
        var motion = TrajectoryEvaluator.evaluate(b, new Vec3(1, 0, 0), 1, 1, MODULES::get);
        b.advanceTrajectory(motion, 1);
        return motion;
    }

    @Test
    void ordinaryAndFetusAccumulateGravityAndExhaustCurvedDistance() {
        for (var source : List.of(TestAttackTypes.BULLET, TestAttackTypes.C_SECTION)) {
            BulletState b = bullet(source, ModTrajectoryModules.GRAVITY.getId());
            step(b);
            step(b);
            assertEquals(-0.1, b.velocity().y, 1e-10);
            assertEquals(-0.15, b.position().y, 1e-10);
            assertEquals(2, b.position().x, 1e-10);
            assertTrue(b.traveled() > 2);
            for (int i = 0; i < 100 && b.traveled() < 40; i++) step(b);
            assertEquals(40, b.traveled(), 1e-9);
            assertTrue(b.position().x < 35);
            assertFalse(b.tickPhysics());
        }
    }

    @Test
    void lasersIgnoreGravityWithoutOverwritingReflection() {
        for (var source : List.of(TestAttackTypes.LASER, TestAttackTypes.BRIMSTONE)) {
            BulletState b = bullet(source, ModTrajectoryModules.GRAVITY.getId());
            step(b);
            assertEquals(new Vec3(1, 0, 0), b.position());
            BulletState mirror = bullet(source, ModTrajectoryModules.MY_REFLECTION_LASER.getId());
            BulletState combined =
                bullet(
                    source,
                    ModTrajectoryModules.GRAVITY.getId(),
                    ModTrajectoryModules.MY_REFLECTION_LASER.getId());
            for (int i = 0; i < 30; i++) {
                step(mirror);
                step(combined);
                assertEquals(mirror.position(), combined.position());
                assertEquals(mirror.traveled(), combined.traveled());
            }
        }
    }

    @Test
    void reflectionAndGravityDoNotAdvanceTwiceAndIgnoreAttachmentOrder() {
        BulletState a =
            bullet(
                TestAttackTypes.BULLET,
                ModTrajectoryModules.GRAVITY.getId(),
                ModTrajectoryModules.MY_REFLECTION_BULLET.getId());
        BulletState b =
            bullet(
                TestAttackTypes.BULLET,
                ModTrajectoryModules.MY_REFLECTION_BULLET.getId(),
                ModTrajectoryModules.GRAVITY.getId());
        step(a);
        step(b);
        assertTrue(a.position().x <= 1.001);
        assertTrue(a.position().y < 0);
        for (int i = 0; i < 20; i++) {
            step(a);
            step(b);
            assertEquals(a.position(), b.position());
        }
    }

    @Test
    void strongerReflectionTurnsBackWithinNormalRange() {
        BulletState b =
            bullet(TestAttackTypes.BULLET, ModTrajectoryModules.MY_REFLECTION_BULLET.getId());
        boolean returning = false;
        for (int i = 0; i < 35; i++) {
            step(b);
            returning |= b.velocity().x < 0;
        }
        assertTrue(returning, "The shot must turn back before spending its normal range");
        assertTrue(b.traveled() < 40);
    }

    @Test
    void gravityResumesFromHomingVelocityWithoutRestoringOldFallSpeed() {
        BulletState b = bullet(TestAttackTypes.BULLET, ModTrajectoryModules.GRAVITY.getId());
        step(b);
        step(b);
        b.setVelocity(new Vec3(0, 1, 0));
        b.setAcceleration(Vec3.ZERO);
        b.tickPhysics();
        step(b);
        assertEquals(0.95, b.velocity().y, 1e-10);
        assertEquals(0, b.velocity().x, 1e-10);
    }
}
