package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackContextFreezeTest {
    @Test
    void frozenContextIgnoresGeometryAndSpawnMutations() throws Exception {
        AttackContext context = PatternTestSupport.context(0.0f, 0.0f, new Vec3(1.0, 2.0, 3.0));
        Vec3 axis = context.getMainAxis();
        context.freeze();

        context.setPos(new Vec3(4.0, 5.0, 6.0));
        context.setMainAxis(new Vec3(1.0, 0.0, 0.0));
        context.useExactSpawnPosition();

        assertTrue(context.isFrozen());
        assertEquals(new Vec3(1.0, 2.0, 3.0), context.getPos());
        assertEquals(axis, context.getMainAxis());
        assertFalse(context.usesExactSpawnPosition());
    }

    @Test
    void copyPreservesFreezeButBuilderDerivationIsMutable() throws Exception {
        AttackContext frozen = PatternTestSupport.context(0.0f, 0.0f);
        frozen.freeze();

        AttackContext copied = frozen.copy();
        AttackContext derived = frozen.toBuilder().build();

        assertTrue(copied.isFrozen());
        assertFalse(derived.isFrozen());
        derived.setPos(new Vec3(2.0, 0.0, 0.0));
        assertEquals(new Vec3(2.0, 0.0, 0.0), derived.getPos());
    }

    @Test
    void builderDerivationAcceptsDirectionOverrides() throws Exception {
        AttackContext source = PatternTestSupport.context(0.0f, 0.0f);
        source.freeze();

        AttackContext derived = source.toBuilder().direction(new Vec3(3.0, 0.0, 0.0)).build();

        assertFalse(derived.isFrozen());
        assertEquals(new Vec3(1.0, 0.0, 0.0), derived.getMainAxis());
    }
}
