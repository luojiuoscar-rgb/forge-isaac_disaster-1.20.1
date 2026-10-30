package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.TinyPlanetReturningMomsKnifeTrajectoryModule;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeTrajectoryTest {
    @Test
    void timestampedSweepPreservesTheProfilesFacingInsteadOfUsingPlayerTranslation() {
        var profile = new TinyPlanetReturningMomsKnifeTrajectoryModule();
        var samples = PlayerMomsKnifeTrajectory.returningSamples(profile, 2, 3, 32, 1, 100, fraction -> {
            var local = profile.sampleLocal(new Vec3(0, 0, 0.6), new Vec3(0, 0, 1), 4, 32, 2 + fraction);
            return PlayerMomsKnifeMotion.blade(local.anchor().add(0, 20 * fraction, 0), local.tip().subtract(local.base()));
        });
        assertEquals(102, samples.get(0).time());
        assertEquals(103, samples.get(samples.size() - 1).time());
        for (var sample : samples) {
            var expected = profile.sampleLocal(new Vec3(0, 0, 0.6), new Vec3(0, 0, 1), 4, 32, sample.time() - 100);
            assertTrue(sample.blade().tip().subtract(sample.blade().base()).normalize()
                    .dot(expected.tip().subtract(expected.base()).normalize()) > 0.99999);
        }
        var trail = new PlayerMomsKnifeTrail();
        trail.append(samples, 103, 3);
        assertNotNull(trail.sample(102.5));
    }

    @Test
    void aWholeFlightSweepKeepsBothOrbitalDirectionsInsteadOfOnlyItsMatchingEndpoints() {
        var profile = new TinyPlanetReturningMomsKnifeTrajectoryModule();
        double duration = 10;
        var path = PlayerMomsKnifeTrajectory.returningSamples(profile, 0, duration, duration, 1, 0,
                fraction -> profile.sampleLocal(new Vec3(0, 0, 0.6), new Vec3(0, 0, 1), 0.05, duration, duration * fraction))
                .stream().map(PlayerMomsKnifeTrail.Sample::blade).toList();
        assertTrue(path.size() >= 73);
        assertTrue(path.stream().anyMatch(blade -> blade.anchor().z < -0.63));
        assertTrue(path.stream().anyMatch(blade -> blade.anchor().x > 0.6));
        assertEquals(new Vec3(0, 0, 0.6), path.get(path.size() - 1).anchor());
    }

    private static final Vec3 FORWARD = new Vec3(0, 0, 1);
    private static final ResourceLocation CUSTOM = ResourceLocation.parse("trajectory_test:knife");

    @Test
    void incompatibleModulesPreserveTheExactBaseSweepIncludingASubTickApex() {
        var bullet = knife(ModTrajectoryModules.HOOK_WORM.getId());
        var path = path(Vec3.ZERO, FORWARD.scale(2), Vec3.ZERO);
        assertSame(path, PlayerMomsKnifeTrajectory.apply(bullet, path, FORWARD, false));
        assertTrue(bullet.getTrajectoryRuntime().states().isEmpty());
    }

    @Test
    void customModuleUsesTheSharedRuntimeAndKeepsTheBaseApex() {
        var bullet = knife(CUSTOM);
        var module = new Shift();
        TrajectoryEvaluator.initialize(bullet, id -> module);
        var result = PlayerMomsKnifeTrajectory.apply(bullet, path(Vec3.ZERO, FORWARD.scale(2), Vec3.ZERO),
                FORWARD, false, id -> module);
        assertEquals(new Vec3(1, 0, 0), result.get(result.size() - 1).anchor());
        assertEquals(2, result.get(1).anchor().z, 1e-10);
        assertEquals(1, ((Shift.State) bullet.getTrajectoryRuntime().states().get(CUSTOM)).calls);
    }

    @Test
    void returningCompletionDocksAtTheOriginalAnchorAndRestoresBladeFacing() {
        var bullet = knife(CUSTOM);
        var module = new Shift();
        var path = path(FORWARD, Vec3.ZERO);
        bullet.setPosition(FORWARD.add(0.3, 0, 0));
        var result = PlayerMomsKnifeTrajectory.apply(bullet, path, FORWARD, true, id -> module);
        var end = result.get(result.size() - 1);
        assertEquals(Vec3.ZERO, end.anchor());
        assertTrue(end.tip().subtract(end.base()).normalize().distanceTo(FORWARD) < 1e-10);
    }

    @Test
    void singleWayRangeClipsTheActualCurvedPathAndItsLastBlade() {
        var path = path(Vec3.ZERO, new Vec3(3, 0, 0), new Vec3(3, 0, 4));
        var clipped = PlayerMomsKnifeTrajectory.limitDistance(path, 5);
        assertEquals(new Vec3(3, 0, 2), clipped.get(clipped.size() - 1).anchor());
        assertEquals(5, PlayerMomsKnifeTrajectory.distance(clipped), 1e-10);
        assertSame(path, PlayerMomsKnifeTrajectory.limitDistance(path, 8));
    }

    @Test
    void sampledTrajectoryPointsParticipateInTheBladeSweep() {
        var bullet = knife(CUSTOM);
        var result = PlayerMomsKnifeTrajectory.apply(bullet, path(Vec3.ZERO, FORWARD), FORWARD,
                false, id -> new Shift(true));
        assertEquals(3, result.size());
        assertEquals(new Vec3(0.5, 1, 0.5), result.get(1).anchor());
        assertEquals(new Vec3(1, 0, 1), result.get(2).anchor());
        assertTrue(PlayerMomsKnifeTrajectory.distance(result) > Math.sqrt(2));
    }

    @Test
    void returningBladeKeepsItsForwardFacingWhileItsVelocityPointsBackwards() {
        var bullet = knife(CUSTOM);
        bullet.setPosition(FORWARD);
        var result = PlayerMomsKnifeTrajectory.apply(bullet, path(FORWARD, Vec3.ZERO), FORWARD,
                false, id -> new Shift());
        var end = result.get(result.size() - 1);
        Vec3 facing = end.tip().subtract(end.base()).normalize();
        assertTrue(facing.distanceTo(new Vec3(-1, 0, 1).normalize()) < 1e-10);
    }

    @Test
    void aVerticalBaseMotionKeepsAllBladeCoordinatesFinite() {
        var bullet = knife(CUSTOM);
        var result = PlayerMomsKnifeTrajectory.apply(bullet, path(Vec3.ZERO, new Vec3(0, 1, 0)),
                new Vec3(0, 1, 0), false, id -> new Shift());
        for (var blade : result) {
            assertTrue(Double.isFinite(blade.anchor().lengthSqr()));
            assertTrue(Double.isFinite(blade.base().lengthSqr()));
            assertTrue(Double.isFinite(blade.tip().lengthSqr()));
        }
    }

    private static BulletState knife(ResourceLocation module) {
        return BulletState.builder().position(Vec3.ZERO).velocity(FORWARD).range(24)
                .attackType(new MomsKnifeAttack(300, 0)).trajectorySpecs(List.of(new TrajectorySpec(module, 0))).build();
    }

    private static List<PlayerMomsKnifeMotion.Blade> path(Vec3... points) {
        return java.util.Arrays.stream(points).map(point -> PlayerMomsKnifeMotion.blade(point, FORWARD)).toList();
    }

    private static final class Shift extends TrajectoryModule<Shift.State> {
        private final boolean sampled;
        Shift() { this(false); }
        Shift(boolean sampled) { this.sampled = sampled; }
        private static final class State implements TrajectoryState {
            int calls;
            public State copy() { var copy = new State(); copy.calls = calls; return copy; }
        }
        public boolean appliesTo(IBulletObject bullet) { return bullet.getRootTypeId().getPath().equals("moms_knife"); }
        public State createState() { return new State(); }
        protected Class<State> stateClass() { return State.class; }
        public void writeState(FriendlyByteBuf buffer, State state) { buffer.writeVarInt(state.calls); }
        public State readState(FriendlyByteBuf buffer) { var state = new State(); state.calls = buffer.readVarInt(); return state; }
        protected TrajectoryMotion applyTyped(TrajectoryContext context, State state) {
            state.calls++;
            Vec3 velocity = context.input.baseVelocity().add(1, 0, 0);
            if (sampled) return new TrajectoryMotion(context.input.position().add(velocity), velocity,
                    Vec3.ZERO, velocity.length(), TrajectoryMotion.CompositionMode.RELATIVE, Double.NaN,
                    List.of(context.input.position().add(velocity.scale(0.5)).add(0, 1, 0),
                            context.input.position().add(velocity)));
            return TrajectoryMotion.relative(context.input.position().add(velocity), velocity, Vec3.ZERO, velocity.length());
        }
    }

}
