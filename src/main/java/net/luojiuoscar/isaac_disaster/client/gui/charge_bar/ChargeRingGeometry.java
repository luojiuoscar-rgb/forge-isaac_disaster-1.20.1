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

    public record Span(int x, int y, int width) {
    }

    private static final int APPROACH_STEPS = 64;
    private static final List<List<Span>> APPROACH_RINGS = createApproachRings();

    /** Pixel coordinates remain integers while radius advances in fine subpixel steps. */
    public static List<Span> rasterizeApproach(float progress) {
        if (!Float.isFinite(progress) || progress <= 0f || progress >= 1f) return List.of();
        return APPROACH_RINGS.get(Math.round(progress * APPROACH_STEPS));
    }

    private static List<List<Span>> createApproachRings() {
        var rings = new ArrayList<List<Span>>();
        for (int step = 0; step <= APPROACH_STEPS; step++) {
            double radius = ChargeRingAnimation.approachRadius((float) step / APPROACH_STEPS);
            double halfWidth = ChargeRingAnimation.APPROACH_RING_WIDTH / 2;
            int extent = (int) Math.ceil(radius + halfWidth);
            var spans = new ArrayList<Span>();
            for (int y = -extent; y < extent; y++) {
                int start = -extent;
                boolean drawing = false;
                for (int x = -extent; x <= extent; x++) {
                    double distance = Math.hypot(x + 0.5, y + 0.5);
                    boolean filled = x < extent && Math.abs(distance - radius) <= halfWidth;
                    if (filled && !drawing) start = x;
                    if (!filled && drawing) spans.add(new Span(start, y, x - start));
                    drawing = filled;
                }
            }
            rings.add(List.copyOf(spans));
        }
        return List.copyOf(rings);
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
