package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpiderMotionDelayTest {
    @Test void wanderWaitsMandatoryPauseThenAdditionalInterval() {
        var timing = new SpiderMotionController.Delay();
        timing.reset(10, 40);
        for (int tick = 1; tick < 50; tick++) assertFalse(timing.tick(false), "tick=" + tick);
        assertTrue(timing.tick(false));
    }

    @Test void followingAndAttackingBypassOnlyTheAdditionalInterval() {
        var timing = new SpiderMotionController.Delay();
        timing.reset(10, 40);
        for (int tick = 1; tick < 10; tick++) assertFalse(timing.tick(true));
        assertTrue(timing.tick(true));
        assertFalse(timing.waiting());
    }

    @Test void anUrgentNeedDuringPendingMovesImmediately() {
        var timing = new SpiderMotionController.Delay();
        timing.reset(10, 40);
        for (int tick = 0; tick < 15; tick++) assertFalse(timing.tick(false));
        assertFalse(timing.waiting());
        assertTrue(timing.tick(true));
    }

    @Test void aNewBurstResetsBothTimers() {
        var timing = new SpiderMotionController.Delay();
        timing.reset(0, 0);
        assertTrue(timing.tick(false));
        timing.reset(6, 10);
        assertTrue(timing.waiting());
        assertFalse(timing.tick(true));
    }
}
