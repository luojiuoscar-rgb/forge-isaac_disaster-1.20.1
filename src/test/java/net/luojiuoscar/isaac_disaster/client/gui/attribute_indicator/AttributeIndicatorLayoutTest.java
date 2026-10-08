package net.luojiuoscar.isaac_disaster.client.gui.attribute_indicator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeIndicatorLayoutTest {
    @Test
    void defaultPlacementPreservesColumnsAndIntegerCenteredRows() {
        var placement = AttributeIndicatorLayout.place(320, 241, 36, 6, 0, 1.0);
        assertEquals(6, placement.left());
        assertEquals(60, placement.top());
        assertEquals(120, placement.height());
        assertEquals(64, placement.width());
    }

    @Test
    void marginsOffsetsAndScaleUseGuiCoordinates() {
        var down = AttributeIndicatorLayout.place(400, 300, 32, 18, 25, 1.5);
        assertEquals(18, down.left());
        assertEquals(85, down.top());
        assertEquals(90, down.width());
        assertEquals(180, down.height());
        var up = AttributeIndicatorLayout.place(400, 300, 32, 18, -25, 0.5);
        assertEquals(95, up.top());
        assertEquals(60, up.height());
    }

    @Test
    void clampsUsingActualTextWidthAndScaledHeight() {
        var bottomRight = AttributeIndicatorLayout.place(320, 240, 80, 4096, 4096, 1.5);
        assertEquals(158, bottomRight.left());
        assertEquals(60, bottomRight.top());
        var top = AttributeIndicatorLayout.place(320, 240, 80, 6, -4096, 1.5);
        assertEquals(0, top.top());
        var oversized = AttributeIndicatorLayout.place(150, 100, 80, 6, 4096, 3.0);
        assertEquals(0, oversized.left());
        assertEquals(0, oversized.top());
        assertEquals(3.0, oversized.scale());
        var onlyTooTall = AttributeIndicatorLayout.place(320, 100, 20, 14, 30, 1.0);
        assertEquals(14, onlyTooTall.left());
        assertEquals(0, onlyTooTall.top());
    }

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
