package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FamiliarRequirementsTest {
    private static final ResourceLocation A = ResourceLocation.parse("test:a");
    private static final ResourceLocation B = ResourceLocation.parse("test:b");
    private static final ResourceLocation C = ResourceLocation.parse("test:c");
    private static final ResourceLocation COMMAND = ResourceLocation.parse("test:command");

    @Test
    void totalsAreDerivedAndRepeatedSourcesMerge() {
        var demands = new FamiliarRequirements();
        demands.addCount(A, 2);
        demands.addCount(B, 3);
        demands.addCount(A, 4);
        assertEquals(6, demands.getCount(A));
        assertEquals(9, demands.getTotal());
        assertEquals(List.of(A, B), List.copyOf(demands.getSources().keySet()));
        assertThrows(UnsupportedOperationException.class, () -> demands.getSources().clear());
    }

    @Test
    void countsNormalizeWithoutOverflowOrChangingOtherSources() {
        var demands = new FamiliarRequirements();
        demands.setCount(A, Integer.MAX_VALUE - 3);
        demands.addCount(B, Integer.MAX_VALUE);
        assertEquals(3, demands.getCount(B));
        assertEquals(Integer.MAX_VALUE, demands.getTotal());
        demands.addCount(A, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE - 3, demands.getCount(A));
        demands.addCount(A, Integer.MIN_VALUE);
        assertEquals(3, demands.getTotal());
        demands.setCount(B, -1);
        assertEquals(0, demands.getTotal());
    }

    @Test
    void commandRemovalUsesItsOwnContributionThenReverseSourceOrder() {
        var demands = new FamiliarRequirements();
        demands.setCount(A, 2);
        demands.setCount(COMMAND, 2);
        demands.setCount(B, 3);
        demands.setCount(C, 4);
        demands.removeCount(COMMAND, 7);
        assertEquals(0, demands.getCount(COMMAND));
        assertEquals(0, demands.getCount(C));
        assertEquals(2, demands.getCount(B));
        assertEquals(2, demands.getCount(A));
        demands.removeCount(COMMAND, Integer.MAX_VALUE);
        assertEquals(0, demands.getTotal());
    }

    @Test
    void persistencePreservesSourceOrderAndNormalizesCounts() {
        var original = new FamiliarRequirements();
        original.setCount(B, 5);
        original.setCount(A, 3);
        var restored = new FamiliarRequirements();
        restored.loadNBT(original.saveNBT());
        assertEquals(original.getSources(), restored.getSources());
        assertEquals(List.of(B, A), List.copyOf(restored.getSources().keySet()));
        restored.clear();
        assertEquals(8, original.getTotal());
    }
}
