package net.luojiuoscar.isaac_disaster.registries.attack_pattern;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.RingAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RingAttackPatternTest {
    private static final double DELTA = 1.0E-4;

    private final AttackPattern pattern = new RingAttackPattern();

    @Test
    void nonPositiveCountsProduceNoContexts() {
        AttackContext reference = testContext();

        assertEquals(List.of(), pattern.generate(new AttackPatternContext(reference, 0)));
        assertEquals(List.of(), pattern.generate(new AttackPatternContext(reference, -1)));
    }

    @Test
    void oneContextKeepsTheReferenceDirection() {
        AttackContext reference = testContext();
        reference.setXRot(-30.0f);
        reference.setYRot(45.0f);

        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 1));

        assertEquals(1, result.size());
        assertVectorEquals(direction(reference), direction(result.get(0)));
        assertEquals(-30.0f, reference.getXRot());
        assertEquals(45.0f, reference.getYRot());
    }

    @Test
    void ringUses360DividedByCountWithoutSpecialCountBranches() {
        AttackContext reference = testContext();

        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 8));

        assertEquals(8, result.size());
        assertVectorEquals(new Vec3(0.0, 0.0, 1.0), direction(result.get(0)));
        assertVectorEquals(new Vec3(-Math.sqrt(0.5), 0.0, Math.sqrt(0.5)), direction(result.get(1)));
        assertVectorEquals(new Vec3(0.0, 0.0, -1.0), direction(result.get(4)));
    }

    @Test
    void ringSupportsArbitraryThreeDimensionalReferenceDirections() {
        AttackContext reference = testContext();
        reference.setXRot(-30.0f);
        reference.setYRot(45.0f);

        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 4));

        assertEquals(4, result.size());
        assertVectorEquals(direction(reference), direction(result.get(0)));
        assertVectorEquals(direction(result.get(0)).scale(-1.0), direction(result.get(2)));
        assertVectorEquals(direction(result.get(1)).scale(-1.0), direction(result.get(3)));
        for (AttackContext context : result) {
            assertEquals(1.0, direction(context).length(), DELTA);
        }
    }

    @Test
    void ringUsesFallbackAxisForVerticalReferenceDirections() {
        AttackContext reference = testContext();
        reference.setXRot(-90.0f);

        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 4));

        assertEquals(4, result.size());
        for (AttackContext context : result) {
            Vec3 direction = direction(context);
            assertEquals(1.0, direction.length(), DELTA);
            assertFalse(Double.isNaN(direction.x));
            assertFalse(Double.isNaN(direction.y));
            assertFalse(Double.isNaN(direction.z));
        }
    }

    @Test
    void generatedContextsAndCopiesDoNotShareMutableTriggerState() {
        AttackContext reference = testContext();
        reference.setXRot(12.0f);
        reference.setYRot(-23.0f);
        reference.setXRotOffset(4.0f);
        reference.setYRotOffset(5.0f);

        AttackContext copied = reference.copy();
        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 2));

        assertNotSame(reference, copied);
        assertNotSame(reference.getTrigger(), copied.getTrigger());
        assertNotSame(result.get(0), result.get(1));
        assertNotSame(result.get(0).getTrigger(), result.get(1).getTrigger());
        assertEquals(16.0f, copied.getXRot());
        assertEquals(-18.0f, copied.getYRot());
        assertEquals(16.0f, reference.getXRot());
        assertEquals(-18.0f, reference.getYRot());
    }

    @Test
    void attackContextSetsAnAbsoluteDirectionFromAVector() {
        AttackContext context = testContext();
        context.setXRotOffset(12.0f);
        context.setYRotOffset(-34.0f);

        context.setDirection(new Vec3(2.0, 0.0, 2.0));

        assertVectorEquals(new Vec3(Math.sqrt(0.5), 0.0, Math.sqrt(0.5)), direction(context));
    }

    @Test
    void attackContextRejectsZeroDirection() {
        assertThrows(IllegalArgumentException.class, () -> testContext().setDirection(Vec3.ZERO));
    }

    private static Vec3 direction(AttackContext context) {
        return Vec3.directionFromRotation(context.getXRot(), context.getYRot()).normalize();
    }

    private static AttackContext testContext() {
        return new AttackContext(
                null,
                null,
                ResourceLocation.fromNamespaceAndPath("test", "bullet"),
                new CompositeTrigger(),
                Map.of(),
                Vec3.ZERO,
                0.0f,
                0.0f
        );
    }

    private static void assertVectorEquals(Vec3 expected, Vec3 actual) {
        assertEquals(expected.x, actual.x, DELTA);
        assertEquals(expected.y, actual.y, DELTA);
        assertEquals(expected.z, actual.z, DELTA);
    }
}
