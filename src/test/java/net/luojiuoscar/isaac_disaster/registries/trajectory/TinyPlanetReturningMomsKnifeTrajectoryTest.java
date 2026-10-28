package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.HeldMomsKnifeGroup;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.TinyPlanetReturningMomsKnifeTrajectoryModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TinyPlanetReturningMomsKnifeTrajectoryTest {
    private final TinyPlanetReturningMomsKnifeTrajectoryModule module = new TinyPlanetReturningMomsKnifeTrajectoryModule();
    private static final Vec3 FORWARD = new Vec3(0, 0, 1);
    private static final Vec3 HELD = FORWARD.scale(0.6);

    @Test
    void reachingTheApexImmediatelyReversesOrbitAndBladeFacing() {
        double duration = 60, apex = duration / 2, epsilon = 1e-6;
        var before = module.sampleLocal(HELD, FORWARD, 4, duration, apex - epsilon);
        var at = module.sampleLocal(HELD, FORWARD, 4, duration, apex);
        var after = module.sampleLocal(HELD, FORWARD, 4, duration, apex + epsilon);
        Vec3 outward = before.tip().subtract(before.base()).normalize();
        Vec3 inward = at.tip().subtract(at.base()).normalize();
        assertTrue(outward.dot(inward) < -0.99999, "Facing switches at the apex instead of turning gradually");
        assertTrue(outward.dot(after.tip().subtract(after.base()).normalize()) < -0.99999);
        assertTrue(PlayerMomsKnifeMotion.unit(at.anchor().subtract(before.anchor()))
                        .dot(PlayerMomsKnifeMotion.unit(after.anchor().subtract(at.anchor()))) < -0.99999,
                "The actual orbit reverses with its blade");
    }
    @Test
    void returningKnivesHaveTheirOwnRegisteredTrajectory() {
        var module = ModTrajectoryModules.resolve(ResourceLocation.parse("isaac_disaster:tiny_planet_returning_moms_knife"));
        assertNotNull(module, "Returning knives need a dedicated trajectory definition");
        assertEquals(100, module.priority());
    }

    @Test
    void flightRestoresTheOriginalRadialProgressPerTurn() {
        for (double range : new double[]{0.001, 1, 4, 8}) for (double speed : new double[]{0.1, 1, 3}) {
            double originalDuration = PlayerMomsKnifeMotion.duration(range, speed);
            double duration = module.duration(originalDuration);
            double oldTurns = Math.max(1, Math.ceil(originalDuration * 2 / 6));
            assertEquals(oldTurns, duration / 5, 1e-9);
            for (double u : new double[]{0, 0.125, 0.5, 0.875, 1}) {
                var blade = module.sampleLocal(HELD, FORWARD, range, duration, duration * u);
                assertEquals(0.6 + 4 * range * u * (1 - u), blade.anchor().length(), 1e-9);
            }
            assertEquals(HELD, module.sampleLocal(HELD, FORWARD, range, duration, duration).anchor());
        }
        assertTrue(module.suppressesHoming());
    }

    @Test
    void rotationUsesExactlyFiveTicksPerCircle() {
        for (double duration : new double[]{10, 20, 30, 60, 110}) {
            double turns = duration / 5;
            double quarterTurn = 1.25;
            var blade = module.sampleLocal(HELD, FORWARD, 2, duration, quarterTurn);
            assertEquals(0, blade.anchor().z, 1e-9);
            assertTrue(blade.anchor().x > 0);
            Vec3 tangent = PlayerMomsKnifeMotion.unit(module.sampleLocal(HELD, FORWARD, 2, duration, quarterTurn + duration * 1e-6).anchor()
                    .subtract(module.sampleLocal(HELD, FORWARD, 2, duration, quarterTurn - duration * 1e-6).anchor()));
            assertTrue(blade.tip().subtract(blade.base()).normalize().dot(tangent) > 0.99999);
            assertEquals(HELD, module.sampleLocal(HELD, FORWARD, 2, duration, duration + 1).anchor());
            assertTrue(module.sweepSegments(0, duration, duration) >= turns * 72);
        }
    }

    @Test
    void everyHeldLayoutReturnsToItsExactSlotAndParallelKnivesKeepTheirSeparation() {
        for (int count : new int[]{1, 2, 3, 16}) for (int slot = 0; slot < count; slot++) {
            var pose = HeldMomsKnifeGroup.layout(count, slot, 0.6);
            Vec3 held = HELD.add(pose.offset());
            var end = module.sampleLocal(held, pose.direction(), 4, 60, 60);
            assertEquals(held, end.anchor());
            assertTrue(Double.isFinite(end.tip().subtract(end.base()).lengthSqr()));
        }
        var left = module.sampleLocal(HELD.add(0.25, 0, 0), FORWARD, 4, 32, 7);
        var right = module.sampleLocal(HELD.add(-0.25, 0, 0), FORWARD, 4, 32, 7);
        assertEquals(0.5, left.anchor().distanceTo(right.anchor()), 1e-9);
    }

    @Test
    void viewingStraightUpOrDownKeepsTheWholeBladeFiniteAndPreservesRadius() {
        var local = module.sampleLocal(HELD, FORWARD, 4, 32, 11);
        for (float pitch : new float[]{-90, -45, 0, 45, 90}) for (float yaw : new float[]{0, 90, 179}) {
            Vec3 position = PlayerMomsKnifeMotion.offsetToWorld(local.anchor(), yaw, pitch);
            assertTrue(Double.isFinite(position.lengthSqr()));
            assertEquals(local.anchor().length(), position.length(), 1e-9);
        }
    }
}
