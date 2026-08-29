package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttackContextBulletSizeSnapshotTest {
    private static final double EPSILON = 1.0E-4;

    @Test
    void resolvesTheSharedSizeFormulaWhenBuilt() throws Exception {
        assertEquals(1.0, builtContext(2.0, 0.0).getBulletScale(), EPSILON);
        assertEquals(2.2360679, builtContext(10.0, 0.0).getBulletScale(), EPSILON);
        assertEquals(3.1622777, builtContext(20.0, 0.0).getBulletScale(), EPSILON);
        assertEquals(2.5, builtContext(2.0, 1.5).getBulletScale(), EPSILON);
    }

    @Test
    void clampsInvalidAndTooSmallComputedSizes() throws Exception {
        assertEquals(0.25, builtContext(0.0, 0.0).getBulletScale(), EPSILON);
        assertEquals(0.25, builtContext(-1.0, 0.0).getBulletScale(), EPSILON);
        assertEquals(0.25, builtContext(2.0, -1.0).getBulletScale(), EPSILON);
        assertEquals(0.25, builtContext(Double.NaN, 0.0).getBulletScale(), EPSILON);
        assertEquals(0.25, builtContext(2.0, Double.POSITIVE_INFINITY).getBulletScale(), EPSILON);
    }

    @Test
    void acceptsModifierRecalculationOrDirectFinalOverride() throws Exception {
        AttackContext recalculated = PatternTestSupport.context(0.0f, 0.0f)
                .toBuilder().damage(2.0).build();
        recalculated.setBulletScale(1.5, false);

        AttackContext overridden = PatternTestSupport.context(0.0f, 0.0f)
                .toBuilder().damage(20.0).build();
        overridden.setBulletScale(0.1, true);

        assertEquals(1.5, recalculated.getBulletScaleModifier(), EPSILON);
        assertEquals(2.5, recalculated.getBulletScale(), EPSILON);
        assertEquals(0.25, overridden.getBulletScale(), EPSILON);

        recalculated.freeze();
        overridden.freeze();
        assertEquals(2.5, recalculated.getBulletScale(), EPSILON);
        assertEquals(0.25, overridden.getBulletScale(), EPSILON);

        recalculated.setBulletScale(9.0, true);
        assertEquals(2.5, recalculated.getBulletScale(), EPSILON);
    }

    @Test
    void derivedContextRetainsModifierButRecalculatesInsteadOfInheritingFinalOverride() throws Exception {
        AttackContext captured = PatternTestSupport.context(0.0f, 0.0f)
                .toBuilder().damage(8.0).bulletScaleModifier(1.5).build();
        captured.setBulletScale(9.0, true);
        AttackContext derived = captured.toBuilder().damage(2.0).build();

        assertEquals(1.5, captured.getBulletScaleModifier());
        assertEquals(1.5, derived.getBulletScaleModifier());
        assertEquals(9.0, captured.getBulletScale(), EPSILON);
        assertEquals(2.5, derived.getBulletScale(), EPSILON);
    }

    private static AttackContext builtContext(double damage, double modifier) throws Exception {
        AttackContext context = PatternTestSupport.context(0.0f, 0.0f)
                .toBuilder().damage(damage).bulletScaleModifier(modifier).build();
        return context;
    }
}
