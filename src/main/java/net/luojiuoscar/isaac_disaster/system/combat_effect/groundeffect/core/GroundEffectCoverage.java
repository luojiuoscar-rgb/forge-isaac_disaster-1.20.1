package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core;

import java.util.ArrayList;
import java.util.List;

/** A complete, immutable seeded outline shared by contact tests and client rendering. */
public final class GroundEffectCoverage {
    private static final int SEGMENTS = 12;
    private static final double EPSILON = 1.0E-10D;
    private final List<Polygon> pieces;

    private GroundEffectCoverage(List<Point> vertices) {
        pieces = vertices.isEmpty() ? List.of() : List.of(new Polygon(vertices));
    }

    public static GroundEffectCoverage puddle(double radius, long shapeSeed) {
        if (!(radius > 0) || !Double.isFinite(radius)) return new GroundEffectCoverage(List.of());
        List<Point> vertices = new ArrayList<>(SEGMENTS);
        for (int i = 0; i < SEGMENTS; i++) {
            double angle = Math.PI * 2 * i / SEGMENTS;
            double r = radius * (0.88D + 0.12D * noise(shapeSeed, i));
            vertices.add(new Point(Math.cos(angle) * r, Math.sin(angle) * r));
        }
        return new GroundEffectCoverage(vertices);
    }

    public List<Polygon> pieces() { return pieces; }
    public boolean isEmpty() { return pieces.isEmpty(); }

    public boolean contains(double x, double z) {
        if (isEmpty() || !Double.isFinite(x) || !Double.isFinite(z)) return false;
        var vertices = pieces.get(0).vertices();
        for (int i = 0; i < vertices.size(); i++) {
            Point a = vertices.get(i);
            Point b = vertices.get((i + 1) % vertices.size());
            if ((b.x - a.x) * (z - a.z) - (b.z - a.z) * (x - a.x) < -EPSILON) return false;
        }
        return true;
    }

    /** A convex outline covers another only when every translated vertex lies inside it. */
    public boolean covers(GroundEffectCoverage other, double offsetX, double offsetZ) {
        if (other == null || other.isEmpty() || isEmpty()
                || !Double.isFinite(offsetX) || !Double.isFinite(offsetZ)) return false;
        if (!contains(offsetX, offsetZ)) return false;
        for (Point point : other.pieces.get(0).vertices()) {
            if (!contains(point.x + offsetX, point.z + offsetZ)) return false;
        }
        return true;
    }

    private static double noise(long seed, int index) {
        long value = seed + index * 0x9E3779B97F4A7C15L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return ((value >>> 11) & 0xFFFFL) / 65535.0D;
    }

    public record Point(double x, double z) { }
    public record Polygon(List<Point> vertices) {
        public Polygon { vertices = List.copyOf(vertices); }
    }
}
