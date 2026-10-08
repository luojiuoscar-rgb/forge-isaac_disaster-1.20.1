package net.luojiuoscar.isaac_disaster.client.gui.attribute_indicator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeIndicatorLayoutTest {
    @Test
    void sixRowsAreCenteredOnScaledGuiHeightWithFixedColumns() {
        assertEquals(120, AttributeIndicatorLayout.HEIGHT);
        assertEquals(6, AttributeIndicatorLayout.ARROW_X);
        assertEquals(14, AttributeIndicatorLayout.ICON_X);
        assertEquals(34, AttributeIndicatorLayout.VALUE_X);
        for (int screenHeight : new int[]{180, 240, 360, 540, 720}) {
            int top = AttributeIndicatorLayout.top(screenHeight);
            assertTrue(Math.abs(top + 60 - screenHeight / 2.0) <= 0.5);
            assertEquals(100, AttributeIndicatorLayout.rowY(screenHeight, 5)
                    - AttributeIndicatorLayout.rowY(screenHeight, 0));
        }
    }
}
