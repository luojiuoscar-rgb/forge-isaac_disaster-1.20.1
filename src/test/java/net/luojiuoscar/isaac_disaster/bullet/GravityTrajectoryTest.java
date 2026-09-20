package net.luojiuoscar.isaac_disaster.bullet;

import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.bullet.core.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GravityTrajectoryTest {
    private static final Map<ResourceLocation, TrajectoryModule> MODULES = Map.of(
            GravityTrajectoryModule.ID, new GravityTrajectoryModule(),
            MyReflectionBulletTrajectoryModule.ID, new MyReflectionBulletTrajectoryModule(),
            MyReflectionLaserTrajectoryModule.ID, new MyReflectionLaserTrajectoryModule());
    private static BulletState bullet(AttackType source, ResourceLocation... ids) {
        return BulletState.builder().position(Vec3.ZERO).velocity(new Vec3(1, 0, 0)).baseSpeed(1)
                .range(40)
                .lifetime(source == TestAttackTypes.LASER || source == TestAttackTypes.BRIMSTONE ? 1000 : 2).attackType(source)
                .trajectorySpecs(Arrays.stream(ids).map(id -> new TrajectorySpec(id, 0)).toList()).build();
    }
    private static TrajectoryMotion step(BulletState b) {
        var motion = TrajectoryEvaluator.evaluate(b, new Vec3(1, 0, 0), 1, 1, MODULES::get);
        b.advanceTrajectory(motion, 1);
        return motion;
    }
    @Test void ordinaryAndFetusAccumulateGravityAndExhaustCurvedDistance() {
        for (var source : List.of(TestAttackTypes.BULLET, TestAttackTypes.C_SECTION)) {
            BulletState b = bullet(source, GravityTrajectoryModule.ID);
            step(b); step(b);
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
    @Test void lasersIgnoreGravityWithoutOverwritingReflection() {
        for (var source : List.of(TestAttackTypes.LASER, TestAttackTypes.BRIMSTONE)) {
            BulletState b = bullet(source, GravityTrajectoryModule.ID);
            step(b);
            assertEquals(new Vec3(1, 0, 0), b.position());
            BulletState mirror = bullet(source, MyReflectionLaserTrajectoryModule.ID);
            BulletState combined = bullet(source, GravityTrajectoryModule.ID, MyReflectionLaserTrajectoryModule.ID);
            for (int i = 0; i < 30; i++) {
                step(mirror); step(combined);
                assertEquals(mirror.position(), combined.position());
                assertEquals(mirror.traveled(), combined.traveled());
            }
        }
    }
    @Test void reflectionAndGravityDoNotAdvanceTwiceAndIgnoreAttachmentOrder() {
        BulletState a = bullet(TestAttackTypes.BULLET, GravityTrajectoryModule.ID, MyReflectionBulletTrajectoryModule.ID);
        BulletState b = bullet(TestAttackTypes.BULLET, MyReflectionBulletTrajectoryModule.ID, GravityTrajectoryModule.ID);
        step(a); step(b);
        assertTrue(a.position().x <= 1.001);
        assertTrue(a.position().y < 0);
        for (int i = 0; i < 20; i++) { step(a); step(b); assertEquals(a.position(), b.position()); }
    }
    @Test void strongerReflectionTurnsBackWithinNormalRange() {
        BulletState b = bullet(TestAttackTypes.BULLET, MyReflectionBulletTrajectoryModule.ID);
        boolean returning = false;
        for (int i = 0; i < 35; i++) { step(b); returning |= b.velocity().x < 0; }
        assertTrue(returning, "The shot must turn back before spending its normal range");
        assertTrue(b.traveled() < 40);
    }

    @Test void gravityResumesFromHomingVelocityWithoutRestoringOldFallSpeed() {
        BulletState b = bullet(TestAttackTypes.BULLET, GravityTrajectoryModule.ID);
        step(b); step(b);
        b.setVelocity(new Vec3(0, 1, 0));
        b.setAcceleration(Vec3.ZERO);
        b.tickPhysics();
        step(b);
        assertEquals(0.95, b.velocity().y, 1e-10);
        assertEquals(0, b.velocity().x, 1e-10);
    }
}
