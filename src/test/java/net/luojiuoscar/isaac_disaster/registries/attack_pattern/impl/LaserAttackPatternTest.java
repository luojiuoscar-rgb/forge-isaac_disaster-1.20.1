package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaserAttackPatternTest {
    @Test
    void preservesFixedSpreadAndLeavesSourceUntouched() throws Exception {
        AttackContext context = PatternTestSupport.context(0.0f, 0.0f);

        List<AttackContext> children = new LaserAttackPattern().generate(new AttackPatternContext(context, 4));

        assertEquals(4, children.size());
        assertEquals(-12.0, PatternTestSupport.yRot(children.get(0)), 1.0E-6);
        assertEquals(-4.0, PatternTestSupport.yRot(children.get(1)), 1.0E-6);
        assertEquals(4.0, PatternTestSupport.yRot(children.get(2)), 1.0E-6);
        assertEquals(12.0, PatternTestSupport.yRot(children.get(3)), 1.0E-6);
        assertEquals(0.0, PatternTestSupport.yRot(context), 1.0E-6);
    }
}
