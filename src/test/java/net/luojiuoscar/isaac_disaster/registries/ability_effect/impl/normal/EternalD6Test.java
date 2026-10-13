package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EternalD6Test {
    @Test
    void oneActivationChoosesExactlyOneBranchAndConsumesOnePlayerRandomDraw() {
        RandomSource source = RandomSource.create(609);
        RandomSource expected = RandomSource.create(609);
        for (int i = 0; i < 100; i++) {
            boolean shouldClear = expected.nextFloat() < 0.25f;
            int[] calls = {0, 0};
            EternalD6.activate(source, () -> calls[0]++, () -> calls[1]++);
            assertEquals(shouldClear ? 0 : 1, calls[0]);
            assertEquals(shouldClear ? 1 : 0, calls[1]);
        }
        assertEquals(expected.nextLong(), source.nextLong());
    }

    @Test
    void playerRandomStreamsAreIndependentAndRepeatable() {
        RandomSource firstPlayer = RandomSource.create(39);
        RandomSource firstControl = RandomSource.create(39);
        RandomSource secondPlayer = RandomSource.create(421);
        for (int i = 0; i < 100; i++) {
            EternalD6.activate(secondPlayer, () -> {}, () -> {});
            int[] actual = {0};
            EternalD6.activate(firstPlayer, () -> actual[0] = 1, () -> actual[0] = 2);
            assertEquals(firstControl.nextFloat() < 0.25f ? 2 : 1, actual[0]);
        }
    }

    @Test
    void singleAndTwoActivationsMatchQuarterAndSevenSixteenthsProbability() {
        RandomSource source = RandomSource.create(609);
        int single = 0;
        int doubled = 0;
        int samples = 100_000;
        for (int i = 0; i < samples; i++) {
            boolean[] cleared = {false, false};
            EternalD6.activate(source, () -> {}, () -> cleared[0] = true);
            EternalD6.activate(source, () -> {}, () -> cleared[1] = true);
            if (cleared[0]) single++;
            if (cleared[0] || cleared[1]) doubled++;
        }
        assertEquals(0.25, (double) single / samples, 0.006);
        assertEquals(0.4375, (double) doubled / samples, 0.006);
    }
}
