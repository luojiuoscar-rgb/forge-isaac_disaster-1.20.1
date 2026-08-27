package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BulletAttackPatternTest {
    @Test
    void preservesTwoShotSideOffsetsWithoutMutatingSource() throws Exception {
        AttackContext context = PatternTestSupport.context(0.0f, 0.0f);
        Vec3 originalPos = context.getPos();

        List<AttackContext> children = new BulletAttackPattern().generate(new AttackPatternContext(context, 2));

        assertEquals(2, children.size());
        assertEquals(-0.25, children.get(0).getPos().x, 1.0E-6);
        assertEquals(0.25, children.get(1).getPos().x, 1.0E-6);
        assertEquals(originalPos.y, children.get(0).getPos().y, 1.0E-6);
        assertEquals(originalPos.z, children.get(0).getPos().z, 1.0E-6);
        assertSame(originalPos, context.getPos());
        assertEquals(0.0, PatternTestSupport.yRot(context), 1.0E-6);
    }

    @Test
    void preservesSymmetricYawSpreadForMultiShot() throws Exception {
        AttackContext context = PatternTestSupport.context(0.0f, 0.0f);

        List<AttackContext> children = new BulletAttackPattern().generate(new AttackPatternContext(context, 5));

        assertEquals(5, children.size());
        assertEquals(-24.0, PatternTestSupport.yRot(children.get(0)), 1.0E-6);
        assertEquals(-12.0, PatternTestSupport.yRot(children.get(1)), 1.0E-6);
        assertEquals(0.0, PatternTestSupport.yRot(children.get(2)), 1.0E-6);
        assertEquals(12.0, PatternTestSupport.yRot(children.get(3)), 1.0E-6);
        assertEquals(24.0, PatternTestSupport.yRot(children.get(4)), 1.0E-6);
        assertEquals(0.0, PatternTestSupport.yRot(context), 1.0E-6);
    }
}
