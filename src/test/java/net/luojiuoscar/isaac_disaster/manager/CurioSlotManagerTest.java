package net.luojiuoscar.isaac_disaster.manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CurioSlotManagerTest {
    @Test
    void capacitiesFloorFinalValuesAndKeepZeroAndTheUpperBound() {
        assertEquals(0, CurioSlotManager.capacity(-10));
        assertEquals(0, CurioSlotManager.capacity(0.99));
        assertEquals(4, CurioSlotManager.capacity(4.99));
        assertEquals(1024, CurioSlotManager.capacity(5000));
    }
}
