package net.luojiuoscar.isaac_disaster.client.gui.attribute_indicator;

import net.luojiuoscar.isaac_disaster.system.attribute_indicator.AttributeSnapshot;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class AttributeIndicatorStateTest {
    @Test
    void formattingKeepsTwoDecimalsAndNormalizesNegativeZeroAcrossLocales() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals("0.00", AttributeIndicatorState.format(-0.004));
            assertEquals("-1.25", AttributeIndicatorState.format(-1.25));
            assertEquals("123.46", AttributeIndicatorState.format(123.456));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void firstSnapshotAndResetEstablishBaselineWithoutFeedback() {
        var state = new AttributeIndicatorState();
        assertFalse(state.hasSnapshot());
        state.accept(snapshot(3.5), false, 100);
        assertTrue(state.hasSnapshot());
        assertEquals(0, state.direction(2, 100));
        state.accept(snapshot(5), false, 200);
        assertEquals(1, state.direction(2, 200));
        state.accept(snapshot(2), true, 300);
        assertEquals(0, state.direction(2, 300));
        state.clear();
        assertFalse(state.hasSnapshot());
        state.accept(snapshot(7), false, 400);
        assertEquals(0, state.direction(2, 400));
    }

    @Test
    void onlyVisibleChangesStartFeedbackAndUnchangedUpdatesDoNotExtendIt() {
        var state = new AttributeIndicatorState();
        state.accept(snapshot(3.5), true, 100);
        state.accept(snapshot(3.501), false, 200);
        assertEquals(0, state.direction(2, 200));
        state.accept(snapshot(3.52), false, 300);
        assertEquals(1, state.direction(2, 2299));
        state.accept(snapshot(3.521), false, 1000);
        assertEquals(0, state.direction(2, 2300));
        assertEquals(0, state.direction(0, 1000));
    }

    @Test
    void oppositeChangeRestartsFeedbackInNewDirection() {
        var state = new AttributeIndicatorState();
        state.accept(snapshot(3.5), true, 0);
        state.accept(snapshot(5), false, 100);
        state.accept(snapshot(4), false, 1000);
        assertEquals(-1, state.direction(2, 2999));
        assertEquals(0, state.direction(2, 3000));
    }

    @Test
    void invalidSnapshotDoesNotReplaceDisplayedState() {
        var state = new AttributeIndicatorState();
        state.accept(snapshot(3.5), true, 0);
        state.accept(snapshot(Double.NaN), false, 100);
        assertEquals("3.50", state.formattedValue(2));
        assertEquals(0, state.direction(2, 100));
    }

    private static AttributeSnapshot snapshot(double damage) {
        return new AttributeSnapshot(0.1, 2.5, damage, 18, 1, 0);
    }
}
