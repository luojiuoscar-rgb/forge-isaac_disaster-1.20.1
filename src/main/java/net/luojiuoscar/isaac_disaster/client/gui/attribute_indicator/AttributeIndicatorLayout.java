package net.luojiuoscar.isaac_disaster.client.gui.attribute_indicator;

import net.luojiuoscar.isaac_disaster.system.attribute_indicator.AttributeSnapshot;

public final class AttributeIndicatorLayout {
    public static final int ICON_SIZE = 16;
    public static final int ROW_HEIGHT = 20;
    public static final int HEIGHT = AttributeSnapshot.SIZE * ROW_HEIGHT;
    public static final int ARROW_X = 6;
    public static final int ICON_X = 14;
    public static final int VALUE_X = 34;

    private AttributeIndicatorLayout() {
    }

    public static int top(int screenHeight) {
        return Math.max(0, (screenHeight - HEIGHT) / 2);
    }

    public static int rowY(int screenHeight, int row) {
        return top(screenHeight) + row * ROW_HEIGHT;
    }
}
