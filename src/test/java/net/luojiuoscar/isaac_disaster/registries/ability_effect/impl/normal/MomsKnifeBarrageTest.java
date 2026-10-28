package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MomsKnifeBarrageTest {
    @Test
    void everyTwoWaitingTicksUnlockAnotherKnifeWithoutTheOldTenKnifeCap() {
        for (int ticks = 0; ticks <= 100; ticks++) {
            assertEquals(2 + ticks / 2, MomsKnifeBarrage.countForWaitTicks(ticks));
        }
        assertEquals(10, MomsKnifeBarrage.countForWaitTicks(16));
        assertEquals(18, MomsKnifeBarrage.countForWaitTicks(32));
    }

    @Test
    void negativeWaitIsSafeAndIntegerOverflowCannotWrapTheCount() {
        assertEquals(2, MomsKnifeBarrage.countForWaitTicks(-1));
        assertEquals(2, MomsKnifeBarrage.countForWaitTicks(Long.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, MomsKnifeBarrage.countForWaitTicks(Long.MAX_VALUE));
    }
}
