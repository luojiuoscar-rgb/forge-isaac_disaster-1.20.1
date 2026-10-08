package net.luojiuoscar.isaac_disaster.client.gui.charge_bar;

import java.util.ArrayList;
import java.util.List;

/** Pixel-art annulus with a transparent center and clockwise angular charge from twelve o'clock. */
public final class ChargeRingGeometry {
    /** Shared diameter in GUI units for every registered charge indicator. */
    public static final int SIZE = 12;

    public enum Part { OUTLINE, TRACK, FILLED }

    public record Pixel(int x, int y, Part part) {
    }

    public static List<Pixel> rasterize(float progress) {
        int size = SIZE;
        double chargeAngle = (Float.isFinite(progress) ? Math.max(0f, Math.min(1f, progress)) : 0f)
                * Math.PI * 2;
        double center = size / 2.0;
        double outerRadius = center - 0.25;
        double innerRadius = size * 0.24;
        var pixels = new ArrayList<Pixel>();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double dx = x + 0.5 - center;
                double dy = y + 0.5 - center;
                double radius = Math.hypot(dx, dy);
                if (radius > outerRadius || radius < innerRadius) continue;
                Part part;
                if (radius >= outerRadius - 0.75 || radius < innerRadius + 0.75) {
                    part = Part.OUTLINE;
                } else {
                    double angle = Math.atan2(dx, -dy);
                    if (angle < 0) angle += Math.PI * 2;
                    part = angle < chargeAngle ? Part.FILLED : Part.TRACK;
                }
                pixels.add(new Pixel(x, y, part));
            }
        }
        return List.copyOf(pixels);
    }

    private ChargeRingGeometry() {
    }
}
