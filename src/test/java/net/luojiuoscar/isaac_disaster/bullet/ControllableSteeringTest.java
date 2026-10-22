package net.luojiuoscar.isaac_disaster.bullet.server;

import java.lang.reflect.Method;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.tracking.TrackingProfile;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControllableSteeringTest {
    private static final Vec3 INITIAL = new Vec3(1, 0, 0);

    @Test
    void aHigherTurningBudgetDoesNotChangeTheDesiredDirection() {
        Vec3 target = new Vec3(0, 0, 64);
        BulletState normal = controlled(1);
        BulletState boosted = controlled(5);
        assertEquals(TrackingProfile.forBullet(normal).controlVelocity(normal, target),
                TrackingProfile.forBullet(boosted).controlVelocity(boosted, target));
    }

    @Test
    void controlPointIsNotOverwrittenByTheLostHomingCruiseFallback() throws Exception {
        BulletState bullet = controlled(1);
        steer(bullet);
        assertTrue(bullet.velocity().z > 0);
    }

    @Test
    void controlTurnMultiplierIncreasesTheTurningBudgetByFive() throws Exception {
        BulletState normal = controlled(1);
        BulletState boosted = controlled(5);
        steer(normal);
        steer(boosted);

        assertEquals(0.1, normal.velocity().distanceTo(INITIAL), 1e-9);
        assertEquals(0.5, boosted.velocity().distanceTo(INITIAL), 1e-9);
    }

    @Test
    void resettingTheMultiplierRestoresNormalControlTurning() throws Exception {
        BulletState bullet = controlled(5);
        steer(bullet);
        assertEquals(0.5, bullet.velocity().distanceTo(INITIAL), 1e-9);
        bullet.setControlTurnMultiplier(1);
        bullet.setVelocity(INITIAL);
        steer(bullet);
        assertEquals(0.1, bullet.velocity().distanceTo(INITIAL), 1e-9);
    }

    @Test
    void controlMultiplierDoesNotChangeHomingTurning() {
        BulletState normal = BulletState.builder().homing(true).velocity(INITIAL)
                .maxSteeringAcceleration(0.1).build();
        BulletState boosted = BulletState.builder().homing(true).velocity(INITIAL)
                .controlTurnMultiplier(5).maxSteeringAcceleration(0.1).build();
        Vec3 target = new Vec3(0, 0, 64);
        for (BulletState bullet : new BulletState[]{normal, boosted}) {
            bullet.setSteeringTarget(target, false);
            bullet.setDesiredVelocity(TrackingProfile.forBullet(bullet).desiredVelocity(bullet, target, false));
            bullet.applySteering();
        }
        assertEquals(normal.velocity(), boosted.velocity());
    }

    @Test
    void aStrongReverseControlTurnDoesNotCancelVelocityAndKillTheBullet() {
        BulletState bullet = BulletState.builder().controllable(true).controlTurnMultiplier(5)
                .velocity(new Vec3(1.25, 0, 0)).maxSteeringAcceleration(0.25).build();
        Vec3 target = new Vec3(-64, 0, 0);
        bullet.setSteeringTarget(target, true);
        bullet.setDesiredVelocity(TrackingProfile.forBullet(bullet).controlVelocity(bullet, target));
        bullet.applySteering();
        assertTrue(bullet.isAlive());
        assertTrue(bullet.velocity().x < 0);
    }

    private static BulletState controlled(double multiplier) {
        return BulletState.builder().controllable(true).velocity(INITIAL).controlTurnMultiplier(multiplier)
                .controlSteer(0.8).maxSteeringAcceleration(0.1).lifetime(100).build();
    }

    private static void steer(BulletState bullet) throws Exception {
        Vec3 target = new Vec3(0, 0, 64);
        bullet.setSteeringTarget(target, true);
        bullet.setDesiredVelocity(TrackingProfile.forBullet(bullet).controlVelocity(bullet, target));
        Method apply = BulletManager.class.getDeclaredMethod("applySteering", BulletState.class);
        apply.setAccessible(true);
        apply.invoke(null, bullet);
    }

}
