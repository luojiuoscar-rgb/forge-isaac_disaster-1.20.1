package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/** One bounded world-space trail; follower progress is measured along its segments. */
final class FlyAttackTrail {
    private static final double EPSILON = 1e-7;
    private static final int MAX_POINTS = 256;
    private final NavigableMap<Double, Vec3> points = new TreeMap<>();

    void start(Vec3 position) {
        points.clear();
        points.put(0.0, position);
    }

    void clear() { points.clear(); }
    double startDistance() { return points.firstKey(); }
    double endDistance() { return points.lastKey(); }
    Vec3 entrance() { return points.firstEntry().getValue(); }

    double append(Vec3 position) {
        var end = points.lastEntry();
        double distance = end.getValue().distanceTo(position);
        if (distance > EPSILON) points.put(end.getKey() + distance, position);
        return endDistance();
    }

    private Vec3 position(double distance) {
        var before = points.floorEntry(distance);
        if (before == null) return entrance();
        var after = points.higherEntry(before.getKey());
        if (after == null) return before.getValue();
        return before.getValue().lerp(after.getValue(),
                (distance - before.getKey()) / (after.getKey() - before.getKey()));
    }

    List<Vec3> steps(double progress, double limit, double speed) {
        List<Vec3> steps = new ArrayList<>();
        double remaining = Math.min(speed, FlyOrbitLayout.MAX_SPEED);
        double end = Math.min(limit, endDistance());
        Vec3 cursor = position(progress);
        while (progress < end - EPSILON && remaining > EPSILON) {
            var next = points.higherEntry(progress + EPSILON);
            if (next == null) break;
            double destination = Math.min(next.getKey(), Math.min(end, progress + remaining));
            Vec3 nextPosition = position(destination);
            steps.add(nextPosition.subtract(cursor));
            remaining -= destination - progress;
            progress = destination;
            cursor = nextPosition;
        }
        return steps;
    }

    void discardBefore(double progress) {
        while (points.size() > 2 && points.higherKey(startDistance()) <= progress) points.pollFirstEntry();
        while (points.size() > MAX_POINTS) points.pollFirstEntry();
    }
}
