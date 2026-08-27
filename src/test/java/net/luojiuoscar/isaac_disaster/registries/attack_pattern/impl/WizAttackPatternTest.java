package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WizAttackPatternTest {
    @Test
    void rotatesCopiesWithoutMutatingTheSourceList() throws Exception {
        AttackContext first = PatternTestSupport.context(0.0f, 0.0f);
        AttackContext second = PatternTestSupport.context(0.0f, 15.0f);
        List<AttackContext> rotated = WizAttackPattern.rotateContexts(List.of(first, second), 45.0f);

        assertEquals(2, rotated.size());
        assertEquals(45.0, PatternTestSupport.yRot(rotated.get(0)), 1.0E-2);
        assertEquals(60.0, PatternTestSupport.yRot(rotated.get(1)), 1.0E-2);
        assertEquals(0.0, PatternTestSupport.yRot(first), 1.0E-2);
        assertEquals(15.0, PatternTestSupport.yRot(second), 1.0E-2);
    }

    @Test
    void handlesNegativeRotationAsWell() throws Exception {
        AttackContext context = PatternTestSupport.context(0.0f, 30.0f, Vec3.ZERO);
        List<AttackContext> rotated = WizAttackPattern.rotateContexts(List.of(context), -45.0f);

        assertEquals(1, rotated.size());
        assertEquals(-15.0, PatternTestSupport.yRot(rotated.get(0)), 1.0E-2);
        assertEquals(30.0, PatternTestSupport.yRot(context), 1.0E-2);
    }
}
