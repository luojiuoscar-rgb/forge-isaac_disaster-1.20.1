package net.luojiuoscar.isaac_disaster.registries.attack_pattern;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.SemicircleAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SemicircleAttackPatternTest {
    private static final double DELTA = 1.0E-4;

    private final AttackPattern pattern = new SemicircleAttackPattern();

    @Test
    void nonPositiveCountsProduceNoContexts() {
        AttackContext reference = testContext();

        assertEquals(List.of(), pattern.generate(new AttackPatternContext(reference, 0)));
        assertEquals(List.of(), pattern.generate(new AttackPatternContext(reference, -1)));
    }

    @Test
    void threeContextsSpanTheSemicircleIncludingItsEndpoints() {
        AttackContext reference = testContext();
        reference.setYRot(180.0f);

        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 3));

        assertEquals(3, result.size());
        assertVectorEquals(new Vec3(-1.0, 0.0, 0.0), direction(result.get(0)));
        assertVectorEquals(new Vec3(0.0, 0.0, -1.0), direction(result.get(1)));
        assertVectorEquals(new Vec3(1.0, 0.0, 0.0), direction(result.get(2)));
    }

    @Test
    void oneContextKeepsTheReferenceDirection() {
        AttackContext reference = testContext();
        reference.setXRot(-30.0f);
        reference.setYRot(45.0f);

        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 1));

        assertEquals(1, result.size());
        assertVectorEquals(direction(reference), direction(result.get(0)));
    }

    @Test
    void semicircleSupportsArbitraryThreeDimensionalReferenceDirections() {
        AttackContext reference = testContext();
        reference.setXRot(-30.0f);
        reference.setYRot(45.0f);

        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 5));

        assertEquals(5, result.size());
        assertVectorEquals(direction(reference), direction(result.get(2)));
        for (AttackContext context : result) {
            assertEquals(1.0, direction(context).length(), DELTA);
        }
    }

    @Test
    void semicircleUsesFallbackAxisForVerticalReferenceDirections() {
        AttackContext reference = testContext();
        reference.setXRot(-90.0f);

        List<AttackContext> result = pattern.generate(new AttackPatternContext(reference, 3));

        assertEquals(3, result.size());
        for (AttackContext context : result) {
            Vec3 direction = direction(context);
            assertEquals(1.0, direction.length(), DELTA);
            assertFalse(Double.isNaN(direction.x));
            assertFalse(Double.isNaN(direction.y));
            assertFalse(Double.isNaN(direction.z));
        }
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
