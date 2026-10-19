package net.luojiuoscar.isaac_disaster.bullet;

import java.util.UUID;
import net.luojiuoscar.isaac_disaster.bullet.core.*;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletStickStateTest {
    private BulletState stuck() {
        var bullet = BulletState.builder().velocity(new Vec3(1, 0, 0)).lifetime(1).range(1).stickDurationTicks(10).build();
        bullet.applyStickSnapshot(new BulletStickState.Snapshot(BulletStickState.Phase.STUCK,
                10, 0, 0, 42, UUID.randomUUID(), new Vec3(0.2, 1, 0)));
        return bullet;
    }

    @Test void attachedMotionDoesNotSpendFlightLifetimeOrRangeAndAllowsStationaryTargets() {
        var bullet = stuck();
        bullet.setVelocity(Vec3.ZERO);
        for (int i = 0; i < 20; i++) bullet.tickAttachedPosition(new Vec3(4, 2, 1));
        assertTrue(bullet.isAlive());
        assertEquals(0, bullet.traveled());
        assertEquals(20, bullet.age());
        assertEquals(new Vec3(4, 2, 1), bullet.position());
        assertTrue(bullet.validateRuntimeVelocity());
    }

    @Test void forcedEndRestoresFlightWithoutResettingRangeOrTrajectoryState() {
        var bullet = stuck();
        bullet.getTrajectoryRuntime().distance(12);
        bullet.endStick(BulletStickEndReason.TARGET_REMOVED);
        assertTrue(bullet.isFlying());
        assertTrue(bullet.isDetached());
        assertEquals(0, bullet.traveled());
        assertEquals(12, bullet.getTrajectoryRuntime().distance());
        assertFalse(bullet.getTrajectorySpecs().isEmpty());
        assertTrue(bullet.velocity().lengthSqr() > 0);
    }

    @Test void invalidVelocityStillRemovesAnAttachedBullet() {
        var invalid = stuck();
        invalid.setVelocity(new Vec3(Double.NaN, 0, 0));
        assertFalse(invalid.isAlive());
    }

    @Test void stickyConfigurationAndSnapshotClampNegativeValues() {
        assertEquals(0, BulletState.builder().build().stickState().durationTicks());
        assertEquals(0, BulletState.builder().stickDurationTicks(-1).build().stickState().durationTicks());
        var snapshot = new BulletStickState.Snapshot(BulletStickState.Phase.STUCK,
                -10, -2, -3, 42, null, new Vec3(Double.NaN, 1, 0));
        assertEquals(0, snapshot.durationTicks());
        assertEquals(0, snapshot.elapsedTicks());
        assertEquals(0, snapshot.revision());
        assertEquals(Vec3.ZERO, snapshot.offset());
    }
    @Test void sampledContactRangeUsesActualSegmentLengths() {
        var bullet = BulletState.builder().velocity(new Vec3(4, 0, 0)).range(10).lifetime(100).build();
        var motion = new TrajectoryMotion(new Vec3(3, 0, 0), new Vec3(2, 0, 0), Vec3.ZERO,
                3, TrajectoryMotion.CompositionMode.ABSOLUTE, 3,
                List.of(new Vec3(2, 0, 0), new Vec3(3, 0, 0)));
        assertTrue(bullet.advanceTrajectory(motion, 1));
        bullet.retainTrajectoryFraction(0.5);
        assertEquals(2, bullet.traveled(), 1e-9);
    }
}
