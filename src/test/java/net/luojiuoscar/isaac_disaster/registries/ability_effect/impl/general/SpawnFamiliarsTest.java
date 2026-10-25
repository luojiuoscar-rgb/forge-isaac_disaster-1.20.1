package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SpawnFamiliarsTest {
    @Test
    void allApprovedRangesIncludeBothEndpointsAndNoOutsideCounts() {
        for (int[] range : new int[][]{{1, 1}, {1, 2}, {2, 4}, {3, 6}, {4, 8}}) {
            var effect = generator(range[0], range[1]);
            RandomSource random = RandomSource.create(12345);
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                int value = effect.sampleCount(random);
                assertTrue(value >= range[0] && value <= range[1]);
                seen.add(value);
            }
            assertTrue(seen.contains(range[0]) && seen.contains(range[1]));
        }
    }

    @Test
    void independentActiveBatchesCanProduceOddTotalsInsteadOfDoublingOneDraw() {
        var effect = generator(2, 4);
        RandomSource random = RandomSource.create(12345);
        Set<Integer> totals = new HashSet<>();
        for (int i = 0; i < 1000; i++) totals.add(effect.sampleCount(random) + effect.sampleCount(random));
        assertEquals(Set.of(4, 5, 6, 7, 8), totals);
    }

    @Test
    void recoverableRangeInputsAreBoundedAndNeverBreakRandomSelection() {
        for (int[] range : new int[][]{{-4, -1}, {8, 2}, {Integer.MAX_VALUE, Integer.MAX_VALUE}}) {
            var effect = generator(range[0], range[1]);
            int value = assertDoesNotThrow(() -> effect.sampleCount(RandomSource.create(1)));
            assertTrue(value >= 0 && value <= 64);
        }
    }

    private static SpawnFamiliars generator(int minimum, int maximum) {
        return new SpawnFamiliars(() -> ResourceLocation.fromNamespaceAndPath("isaac_disaster", "blue_fly"),
                minimum, maximum, false);
    }
}
