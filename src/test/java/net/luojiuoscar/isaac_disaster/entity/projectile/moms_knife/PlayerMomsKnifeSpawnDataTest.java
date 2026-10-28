package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMomsKnifeSpawnDataTest {
    @Test
    void lateTrackingIncludesBoundedWorldSpaceHistory() {
        var trail = List.of(new PlayerMomsKnifeTrail.Sample(875.25,
                PlayerMomsKnifeMotion.blade(new Vec3(80, 65, -20), new Vec3(1, 0, 0))),
                new PlayerMomsKnifeTrail.Sample(876,
                        PlayerMomsKnifeMotion.blade(new Vec3(81, 65, -20), new Vec3(0, 1, 0)), false));
        checkRoundTrip(new PlayerMomsKnifeSpawnData((byte) 0, 42, new Vec3(0, 0, 1), 8, 1, 872, 0, 0,
                0, Vec3.ZERO, new Vec3(0, 0, 1), ModAttackTypes.MOMS_KNIFE.getId(),
                Vec3.ZERO, Vec3.ZERO, "isaac_disaster:tiny_planet_returning_moms_knife", 2, trail, true, false));
    }
    @Test
    void anOrbitingMainKnifeCarriesItsFrozenTrajectoryAndDuration() {
        checkRoundTrip(new PlayerMomsKnifeSpawnData((byte) 0, 42, new Vec3(0, 0, 1), 8, 1, 872, 0, 0,
                3, new Vec3(0.25, 0, 0), new Vec3(0, 0, 1), ModAttackTypes.MOMS_KNIFE.getId(),
                Vec3.ZERO, Vec3.ZERO, "isaac_disaster:tiny_planet_returning_moms_knife", 2, List.of(), false, false));
    }
    @Test
    void aDetachedShotCarriesItsRealDirectionAndOwnerInTheInitialPayload() {
        var source = new PlayerMomsKnifeSpawnData((byte) 1, 765, new Vec3(-0.3, 0.5, -0.8), 24, 1.7f, 0, 159, -32,
                0, Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD, ModAttackTypes.MOMS_KNIFE.getId(),
                Vec3.ZERO, Vec3.ZERO, "", 0, List.of(), false, false);
        checkRoundTrip(source);
    }

    @Test
    void aMainKnifeCarriesItsFlightClockAndVerticalHeading() {
        var source = new PlayerMomsKnifeSpawnData((byte) 0, 42, new Vec3(0, 1, 0), 6, 0.5f, 872, 37, -90,
                0, Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD, ModAttackTypes.MOMS_KNIFE.getId(),
                Vec3.ZERO, Vec3.ZERO, "", 0, List.of(), false, false);
        checkRoundTrip(source);
    }

    @Test
    void anIndependentReturningKnifeCarriesItsSourceAndDiscardFlag() {
        checkRoundTrip(new PlayerMomsKnifeSpawnData((byte) 0, 42, new Vec3(0, 0, 1), 8, 1, 872, 0, 0,
                0, Vec3.ZERO, PlayerMomsKnifeMotion.FORWARD, ModAttackTypes.MOMS_KNIFE_WEAPON.getId(),
                Vec3.ZERO, Vec3.ZERO, "", 32, List.of(), false, true));
    }

    @Test
    void enteringViewMidFlightCarriesTheTrajectoryPoseWithoutDefaultInterpolation() {
        var source = new PlayerMomsKnifeSpawnData((byte) 0, 42, new Vec3(0, 0, 1), 6, 1, 872, 0, 0,
                3, new Vec3(0.25, 0, 0), new Vec3(0, 0, 1), ModAttackTypes.MOMS_KNIFE.getId(),
                new Vec3(0.4, -0.2, 0.1), new Vec3(1, 0, 1).normalize(), "", 0, List.of(), false, false);
        checkRoundTrip(source);
    }

    private static void checkRoundTrip(PlayerMomsKnifeSpawnData source) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            source.write(buffer);
            var result = PlayerMomsKnifeSpawnData.read(buffer);
            assertEquals(source.mode(), result.mode());
            assertEquals(source.owner(), result.owner());
            assertTrue(source.direction().distanceTo(result.direction()) < 1e-12);
            assertEquals(source.range(), result.range());
            assertEquals(source.speed(), result.speed());
            assertEquals(source.launchedAt(), result.launchedAt());
            assertEquals(source.yaw(), result.yaw());
            assertEquals(source.pitch(), result.pitch());
            assertEquals(source.trajectoryOffset(), result.trajectoryOffset());
            assertEquals(source.trajectoryDirection(), result.trajectoryDirection());
            assertEquals(source.returningTrajectory(), result.returningTrajectory());
            assertEquals(source.flightDuration(), result.flightDuration());
            assertEquals(source.fastMoving(), result.fastMoving());
            assertEquals(source.discardOnReturn(), result.discardOnReturn());
            assertEquals(source.trail().size(), result.trail().size());
            for (int i = 0; i < source.trail().size(); i++) {
                assertEquals(source.trail().get(i).time(), result.trail().get(i).time());
                assertEquals(source.trail().get(i).connected(), result.trail().get(i).connected());
                assertEquals(source.trail().get(i).blade().anchor(), result.trail().get(i).blade().anchor());
                assertTrue(source.trail().get(i).blade().tip().distanceTo(result.trail().get(i).blade().tip()) < 1e-12);
            }
            assertEquals(0, buffer.readableBytes());
        } finally { buffer.release(); }
    }
}
