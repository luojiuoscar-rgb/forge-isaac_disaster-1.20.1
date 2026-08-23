package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitSequenceTest {
    @Test
    void emptySequenceCopiesIndependently() {
        SplitSequence source = new SplitSequence();
        SplitSequence copy = source.copy();

        assertNotSame(source, copy);
        assertTrue(source.isEmpty());
        assertTrue(copy.isEmpty());
    }

    @Test
    void attackContextCopiesSplitSequenceIndependently() {
        AttackContext source = new AttackContext(
                null, null, ResourceLocation.fromNamespaceAndPath("test", "color"),
                new CompositeTrigger(), Map.of(), Vec3.ZERO, 0.0f, 0.0f);
        SplitSequence sequence = new SplitSequence();
        source.setSplitSequence(sequence);

        AttackContext copy = source.copy();

        assertNotSame(source.getSplitSequence(), copy.getSplitSequence());
        assertTrue(copy.getSplitSequence().isEmpty());
    }

    @Test
    void splitRuleCacheRejectsNullArguments() {
        assertThrows(NullPointerException.class,
                () -> SplitRuleCache.allows(null, null, null));
    }
}
