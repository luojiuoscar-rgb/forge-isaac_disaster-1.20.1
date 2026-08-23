package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
                new ArmorStand(null, 0.0, 0.0, 0.0), null,
                ResourceLocation.fromNamespaceAndPath("test", "color"),
                new CompositeTrigger(), Map.of(), Vec3.ZERO, 0.0f, 0.0f);
        SplitSequence sequence = new SplitSequence();
        source.setSplitSequence(sequence);

        AttackContext copy = source.copy();

        assertNotSame(source.getSplitSequence(), copy.getSplitSequence());
        assertTrue(copy.getSplitSequence().isEmpty());
    }

    @Test
    void splitTriggerCountsIncrementByTriggerType() {
        SplitTriggerCounts counts = new SplitTriggerCounts();
        counts.increment(SplitTriggerType.BLOCK);
        counts.increment(SplitTriggerType.ENTITY);
        counts.increment(SplitTriggerType.END_OF_LIFE);

        assertEquals(1, counts.getBlockHits());
        assertEquals(1, counts.getEntityHits());
        assertEquals(1, counts.getEndOfLife());
    }

}
