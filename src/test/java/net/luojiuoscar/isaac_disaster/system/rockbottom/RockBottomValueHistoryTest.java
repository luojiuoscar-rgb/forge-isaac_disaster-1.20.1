package net.luojiuoscar.isaac_disaster.system.rockbottom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RockBottomValueHistoryTest {
    @Test
    void missingHistoryUsesTheCurrentFinalValue() {
        assertEquals(12.0, RockBottomValueHistory.resolve(12.0, null));
    }

    @Test
    void higherCurrentValueReplacesTheHistoricalMaximum() {
        assertEquals(14.0, RockBottomValueHistory.resolve(14.0, 12.0));
    }

    @Test
    void lowerOrEqualCurrentValueKeepsTheHistoricalMaximum() {
        assertEquals(12.0, RockBottomValueHistory.resolve(8.0, 12.0));
        assertEquals(12.0, RockBottomValueHistory.resolve(12.0, 12.0));
    }
}
