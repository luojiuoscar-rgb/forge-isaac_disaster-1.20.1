package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.MomsKnifeAttack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrajectoryApplicabilityTest {
    private static final List<String> ALL = List.of("wiggle_worm", "hook_worm", "ring_worm", "ouroboros_worm",
            "tiny_planet_bullet", "tiny_planet_laser", "my_reflection_bullet", "my_reflection_laser", "gravity");

    @Test
    void everyExistingTrajectoryRejectsKnivesEvenWhenDirectlyAttached() {
        var knife = knife();
        for (String name : ALL) {
            var id = ResourceLocation.parse("isaac_disaster:" + name);
            var module = ModTrajectoryModules.resolve(id);
            assertNotNull(module);
            assertFalse(module.appliesTo(knife), name);
            knife.getTrajectoryRuntime().addSpec(new TrajectorySpec(id, 0));
        }
        var motion = TrajectoryEvaluator.evaluate(knife, new Vec3(0, 0, 1), 1, 1, ModTrajectoryModules::resolve);
        assertEquals(new Vec3(0, 0, 1), motion.desiredPosition());
        assertTrue(knife.getTrajectoryRuntime().states().isEmpty());
    }

    @Test
    void wormsContinueToAcceptBulletAndLaserRootsIncludingTheirVariants() {
        for (String name : ALL.subList(0, 4)) {
            var module = ModTrajectoryModules.resolve(ResourceLocation.parse("isaac_disaster:" + name));
            for (var type : TestAttackTypes.PROJECTILES) {
                assertTrue(module.appliesTo(BulletState.builder().attackType(type).build()), name + " " + type.getId());
            }
            var otherRoot = ResourceLocation.parse("trajectory_test:other_attack");
            assertFalse(module.appliesTo(BulletState.builder().typeIds(otherRoot, otherRoot).build()), name);
        }
    }

    @Test
    void initializationAlsoSkipsIncompatibleModules() {
        var knife = knife();
        for (String name : ALL) knife.getTrajectoryRuntime().addSpec(
                new TrajectorySpec(ResourceLocation.parse("isaac_disaster:" + name), 0));
        TrajectoryEvaluator.initialize(knife, ModTrajectoryModules::resolve);
        assertTrue(knife.getTrajectoryRuntime().states().isEmpty());
    }

    private static BulletState knife() {
        return BulletState.builder().position(Vec3.ZERO).velocity(new Vec3(0, 0, 1))
                .range(24).attackType(new MomsKnifeAttack(300, 0)).build();
    }
}
