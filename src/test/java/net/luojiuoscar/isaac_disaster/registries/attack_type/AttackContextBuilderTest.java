package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Requires a Forge-launched test environment to bootstrap vanilla registries. */
@Disabled("Forge/Minecraft registry bootstrap is unavailable in the plain Gradle test worker")
class AttackContextBuilderTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
    }

    @Test
    void resolvesCreationTimeDefaultsAndSanitizesScalars() {
        ArmorStand owner = owner();
        AttackContext context = AttackContext.builder(owner, null)
                .position(Vec3.ZERO)
                .rotation(0.0f, 0.0f)
                .range(0.0)
                .speed(Double.NaN)
                .build();

        assertEquals(owner, context.getShooter());
        assertEquals(AttackContext.DEFAULT_RANGE, context.getBulletRange());
        assertEquals(AttackContext.DEFAULT_SPEED, context.getBulletSpeed());
        assertTrue(Double.isFinite(context.getDamage()));
    }

    @Test
    void copiesMutableInputsAndCreatesIndependentSnapshots() {
        ArmorStand owner = owner();
        CompositeTrigger trigger = new CompositeTrigger();
        Map<ResourceLocation, Integer> trajectories = new HashMap<>();
        trajectories.put(ResourceLocation.fromNamespaceAndPath("test", "base"), 1);
        SplitSequence sequence = new SplitSequence();

        AttackContext context = AttackContext.builder(owner, null)
                .trigger(trigger)
                .trajectories(trajectories)
                .splitSequence(sequence)
                .build();

        trajectories.clear();
        assertEquals(1, context.getTrajectories().size());
        assertNotSame(trigger, context.getTrigger());
        assertNotSame(sequence, context.getSplitSequence());
        assertNotSame(context.getTrigger(), context.copyTrigger());
    }

    @Test
    void toBuilderResolvesNewScalarValuesWithoutMutatingOriginal() {
        ArmorStand owner = owner();
        AttackContext original = AttackContext.builder(owner, null)
                .position(Vec3.ZERO)
                .rotation(0.0f, 0.0f)
                .damage(4.0)
                .range(10.0)
                .speed(2.0)
                .build();

        AttackContext copy = original.toBuilder().damage(2.0).range(5.0).speed(3.0).build();
        assertEquals(4.0f, original.getDamage());
        assertEquals(10.0, original.getBulletRange());
        assertEquals(2.0, original.getBulletSpeed());
        assertEquals(2.0f, copy.getDamage());
        assertEquals(5.0, copy.getBulletRange());
        assertEquals(3.0, copy.getBulletSpeed());
    }

    private static ArmorStand owner() {
        return new ArmorStand(null, 0.0, 0.0, 0.0);
    }
}
