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

    /** Bounds include the arrow reserve and the widest currently displayed value. */
    public static Placement place(int screenWidth, int screenHeight, int textWidth,
                                  int leftMargin, int verticalOffset, double scale) {
        double width = (VALUE_X - ARROW_X + textWidth) * scale;
        double height = HEIGHT * scale;
        double left = width > screenWidth ? 0 : clamp(leftMargin, 0, screenWidth - width);
        // Keep the existing integer-centered default origin on odd GUI heights.
        double centeredTop = Math.floor((screenHeight - height) / 2.0);
        double top = height > screenHeight ? 0 : clamp(centeredTop + verticalOffset, 0, screenHeight - height);
        return new Placement(left, top, width, height, scale);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Placement(double left, double top, double width, double height, double scale) {
    }
}
