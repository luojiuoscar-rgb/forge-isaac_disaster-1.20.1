package net.luojiuoscar.isaac_disaster.groundeffect.core;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Immutable convex pieces of a painted footprint, in coordinates relative to its puddle. */
public final class GroundEffectCoverage {
    public static final int MAX_SERIALIZED_BYTES = 32 * 1024 * 1024 - 65536;
    private static final int SEGMENTS = 12;
    private static final double EPSILON = 1.0E-10D;
    private final List<Polygon> pieces;
    private final boolean uncut;
    private final long serializedBytes;

    private GroundEffectCoverage(List<Polygon> pieces, boolean uncut) {
        this.pieces = List.copyOf(pieces);
        this.uncut = uncut;
        long bytes = 1;
        if (!uncut) {
            bytes += varIntBytes(pieces.size());
            for (Polygon polygon : pieces) bytes += varIntBytes(polygon.vertices.size()) + 16L * polygon.vertices.size();
        }
        this.serializedBytes = bytes;
    }

    public long serializedBytes() { return serializedBytes; }

    private static int varIntBytes(int value) {
        int bytes = 1;
        while ((value & ~127) != 0) { value >>>= 7; bytes++; }
        return bytes;
    }

    public static GroundEffectCoverage puddle(double radius, long shapeSeed) {
        if (!(radius > 0.0D)) return new GroundEffectCoverage(List.of(), true);
        List<Point> points = new ArrayList<>(SEGMENTS);
        for (int i = 0; i < SEGMENTS; i++) {
            double angle = Math.PI * 2.0D * i / SEGMENTS;
            double r = radius * (0.88D + 0.12D * noise(shapeSeed, i));
            points.add(new Point(Math.cos(angle) * r, Math.sin(angle) * r));
        }
        return new GroundEffectCoverage(List.of(new Polygon(points)), true);
    }

    public List<Polygon> pieces() { return pieces; }
    public boolean isEmpty() { return pieces.isEmpty(); }

    public boolean contains(double x, double z) {
        for (Polygon piece : pieces) {
            boolean inside = true;
            for (int i = 0; i < piece.vertices.size(); i++) {
                Point a = piece.vertices.get(i);
                Point b = piece.vertices.get((i + 1) % piece.vertices.size());
                if ((b.x - a.x) * (z - a.z) - (b.z - a.z) * (x - a.x) < -EPSILON) {
                    inside = false;
                    break;
                }
            }
            if (inside) return true;
        }
        return false;
    }

    /** Subtract a convex fresh brush; erased regions never retain a reference to that brush. */
    public GroundEffectCoverage subtract(GroundEffectCoverage brush, double offsetX, double offsetZ) {
        List<Polygon> remaining = pieces;
        for (Polygon source : brush.pieces) {
            Polygon cutter = source.translated(offsetX, offsetZ);
            List<Polygon> next = new ArrayList<>();
            for (Polygon piece : remaining) subtract(piece, cutter, next);
            remaining = next;
        }
        return remaining.equals(pieces) ? this : new GroundEffectCoverage(remaining, false);
    }

    private static void subtract(Polygon source, Polygon cutter, List<Polygon> output) {
        // Reject disjoint pieces before splitting, so nearby paint does not fragment untouched areas.
        Polygon intersection = source;
        for (int i = 0; i < cutter.vertices.size() && intersection != null; i++) {
            intersection = clip(intersection, cutter.vertices.get(i),
                    cutter.vertices.get((i + 1) % cutter.vertices.size()), true);
        }
        if (intersection == null) {
            output.add(source);
            return;
        }

        Polygon inside = source;
        for (int i = 0; i < cutter.vertices.size() && inside != null; i++) {
            Point a = cutter.vertices.get(i);
            Point b = cutter.vertices.get((i + 1) % cutter.vertices.size());
            Polygon outside = clip(inside, a, b, false);
            if (outside != null) output.add(outside);
            inside = clip(inside, a, b, true);
        }
    }

    private static @Nullable Polygon clip(Polygon polygon, Point a, Point b, boolean keepInside) {
        List<Point> clipped = new ArrayList<>();
        Point previous = polygon.vertices.get(polygon.vertices.size() - 1);
        double previousSide = side(a, b, previous);
        boolean previousKept = keepInside ? previousSide >= 0.0D : previousSide <= 0.0D;
        for (Point current : polygon.vertices) {
            double currentSide = side(a, b, current);
            boolean currentKept = keepInside ? currentSide >= 0.0D : currentSide <= 0.0D;
            if (currentKept != previousKept) {
                double t = previousSide / (previousSide - currentSide);
                clipped.add(new Point(previous.x + t * (current.x - previous.x),
                        previous.z + t * (current.z - previous.z)));
            }
            if (currentKept) clipped.add(current);
            previous = current;
            previousSide = currentSide;
            previousKept = currentKept;
        }
        if (clipped.size() < 3 || area(clipped) <= EPSILON) return null;
        if (clipped.equals(polygon.vertices)) return polygon;
        return new Polygon(clipped);
    }

    private static double side(Point a, Point b, Point point) {
        return (b.x - a.x) * (point.z - a.z) - (b.z - a.z) * (point.x - a.x);
    }

    private static double area(List<Point> points) {
        double twiceArea = 0;
        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            twiceArea += a.x * b.z - a.z * b.x;
        }
        return twiceArea * 0.5D;
    }

    private static double noise(long seed, int index) {
        long value = seed + index * 0x9E3779B97F4A7C15L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return ((value >>> 11) & 0xFFFFL) / 65535.0D;
    }

    /** Uncut circles retain the compact radius/seed representation in snapshots. */
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(uncut);
        if (uncut) return;
        buf.writeCollection(pieces, (buffer, polygon) -> buffer.writeCollection(polygon.vertices,
                (vertices, point) -> { vertices.writeDouble(point.x); vertices.writeDouble(point.z); }));
    }

    public static GroundEffectCoverage read(FriendlyByteBuf buf, double radius, long shapeSeed) {
        if (buf.readBoolean()) return puddle(radius, shapeSeed);
        int pieceCount = buf.readVarInt();
        if (pieceCount < 0 || pieceCount > buf.readableBytes() / 49) {
            throw new IllegalArgumentException("Invalid footprint piece count");
        }
        List<Polygon> pieces = new ArrayList<>(pieceCount);
        for (int i = 0; i < pieceCount; i++) {
            int vertexCount = buf.readVarInt();
            if (vertexCount < 3 || vertexCount > buf.readableBytes() / 16) {
                throw new IllegalArgumentException("Invalid footprint vertex count");
            }
            List<Point> vertices = new ArrayList<>(vertexCount);
            for (int vertex = 0; vertex < vertexCount; vertex++) vertices.add(new Point(buf.readDouble(), buf.readDouble()));
            pieces.add(new Polygon(vertices));
        }
        return new GroundEffectCoverage(pieces, false);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GroundEffectCoverage coverage && pieces.equals(coverage.pieces);
    }

    @Override
    public int hashCode() { return pieces.hashCode(); }

    public record Point(double x, double z) {
        public Point {
            if (!Double.isFinite(x) || !Double.isFinite(z)) throw new IllegalArgumentException("Nonfinite footprint vertex");
        }
    }

    public record Polygon(List<Point> vertices) {
        public Polygon {
            vertices = List.copyOf(vertices);
            if (vertices.size() < 3) throw new IllegalArgumentException("Footprint needs at least three vertices");
        }

        private Polygon translated(double x, double z) {
            return new Polygon(vertices.stream().map(point -> new Point(point.x + x, point.z + z)).toList());
        }
    }
}
