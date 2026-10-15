package net.luojiuoscar.isaac_disaster.system.heart;

import net.luojiuoscar.isaac_disaster.capability.entity.ExtraData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EternalHeartStateTest {
    @Test
    void firstHeartStartsTheCountdown() {
        ExtraData data = new ExtraData();

        assertFalse(EternalHeartState.grant(data));
        assertTrue(EternalHeartState.has(data));
        assertEquals(EternalHeartState.DURATION_TICKS, EternalHeartState.remainingTicks(data));
    }

    @Test
    void secondHeartConvertsThePairImmediately() {
        ExtraData data = new ExtraData();
        EternalHeartState.grant(data);

        assertTrue(EternalHeartState.grant(data));
        assertFalse(EternalHeartState.has(data));
    }

    @Test
    void countdownConvertsThePendingHeartAtTheEnd() {
        ExtraData data = new ExtraData();
        EternalHeartState.grant(data);

        for (int tick = 0; tick < EternalHeartState.DURATION_TICKS - 1; tick++) {
            assertFalse(EternalHeartState.tick(data));
        }
        assertTrue(EternalHeartState.tick(data));
        assertFalse(EternalHeartState.has(data));
    }

    @Test
    void damageConsumesThePendingHeart() {
        ExtraData data = new ExtraData();
        EternalHeartState.grant(data);

        assertTrue(EternalHeartState.consume(data));
        assertFalse(EternalHeartState.has(data));
        assertFalse(EternalHeartState.consume(data));
    }
}
