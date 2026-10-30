package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackContextFreezeTest {
    @Test
    void shortPositiveRangesSurviveConstructionAndCopies() throws Exception {
        for (double range : new double[]{0.00001, 0.05, 0.65, 1, 24, 64}) {
            AttackContext context = PatternTestSupport.context(0, 0).toBuilder().range(range).build();
            assertEquals(range, context.getBulletRange(), 1e-12);
            assertEquals(range, context.copy().getBulletRange(), 1e-12);
            assertEquals(range, context.toBuilder().build().getBulletRange(), 1e-12);
        }
    }

    @Test
    void invalidRangesStillUseTheDefaultAndOversizedRangesRemainBounded() throws Exception {
        for (double range : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(AttackContext.DEFAULT_RANGE,
                    PatternTestSupport.context(0, 0).toBuilder().range(range).build().getBulletRange());
        }
        assertEquals(AttackContext.MAX_RANGE,
                PatternTestSupport.context(0, 0).toBuilder().range(100).build().getBulletRange());
    }

    @Test
    void frozenContextIgnoresGeometryAndSpawnMutations() throws Exception {
        AttackContext context = PatternTestSupport.context(0.0f, 0.0f, new Vec3(1.0, 2.0, 3.0));
        Vec3 axis = context.getMainAxis();
        context.freeze();

        context.setPos(new Vec3(4.0, 5.0, 6.0));
        context.setMainAxis(new Vec3(1.0, 0.0, 0.0));
        context.useFixedLaunchTransform();

        assertTrue(context.isFrozen());
        assertEquals(new Vec3(1.0, 2.0, 3.0), context.getPos());
        assertEquals(axis, context.getMainAxis());
        assertFalse(context.usesFixedLaunchTransform());
    }

}
