package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Timestamped world-space blades; replaying them never advances the attack simulation. */
public final class PlayerMomsKnifeTrail {
    public static final int MAX_SAMPLES = 1024;
    public static final int LIFETIME = 3;
    public static final double SPEED_THRESHOLD = 1.5;
    public record Sample(double time, PlayerMomsKnifeMotion.Blade blade, boolean connected) {
        public Sample(double time, PlayerMomsKnifeMotion.Blade blade) { this(time, blade, true); }
    }
    private final List<Sample> samples = new ArrayList<>();
    private final List<Segment> segments = new ArrayList<>();
    @Nullable private AABB bounds;
    private boolean boundsDirty;

    public List<Sample> samples() { return List.copyOf(samples); }
    public void clear() { samples.clear(); segments.clear(); bounds = null; boundsDirty = false; }

    static List<Sample> timed(List<PlayerMomsKnifeMotion.Blade> path, double from, double to) {
        List<Sample> result = new ArrayList<>(path.size());
        for (int i = 0; i < path.size(); i++) {
            result.add(new Sample(from + (to - from) * i / Math.max(1, path.size() - 1), path.get(i)));
        }
        return result;
    }

    boolean recordMotion(List<Sample> path, Vec3 fromOwner, Vec3 toOwner, double now) {
        boolean fast = speed(path, fromOwner, toOwner) > SPEED_THRESHOLD;
        recordPath(path, now, fast);
        return fast;
    }

    static double speed(List<Sample> path, Vec3 fromOwner, Vec3 toOwner) {
        if (path.size() < 2) return 0;
        double start = path.get(0).time(), duration = path.get(path.size() - 1).time() - start;
        if (!(duration > 0) || !Double.isFinite(duration)) return 0;
        Vec3 previous = null;
        double distance = 0;
        for (Sample sample : path) {
            Vec3 center = sample.blade().base().lerp(sample.blade().tip(), 0.5)
                    .subtract(fromOwner.lerp(toOwner, (sample.time() - start) / duration));
            if (previous != null) distance += center.distanceTo(previous);
            previous = center;
        }
        return Double.isFinite(distance) ? distance / duration : 0;
    }

    void recordPath(List<Sample> path, double now, boolean fast) {
        if (fast && path.size() > 1 && path.get(path.size() - 1).time() > path.get(0).time()) {
            List<Sample> connectedPath = new ArrayList<>(path);
            Sample first = path.get(0);
            boolean continuous = !samples.isEmpty()
                    && Math.abs(samples.get(samples.size() - 1).time() - first.time()) < 1e-7;
            connectedPath.set(0, new Sample(first.time(), first.blade(), continuous));
            append(connectedPath, now, LIFETIME);
        } else trim(now, LIFETIME);
    }

    public void append(List<Sample> path, double now, double lifetime) {
        for (Sample sample : path) {
            if (!Double.isFinite(sample.time()) || !Double.isFinite(sample.blade().anchor().lengthSqr())) continue;
            if (!samples.isEmpty() && sample.time() <= samples.get(samples.size() - 1).time()) continue;
            var before = samples.isEmpty() || !sample.connected() ? sample.blade() : samples.get(samples.size() - 1).blade();
            samples.add(sample);
            segments.add(new Segment(before, sample.blade()));
            boundsDirty = true;
        }
        trim(now, lifetime);
        if (samples.size() > MAX_SAMPLES) removePrefix(samples.size() - MAX_SAMPLES);
    }

    public void trim(double now, double lifetime) {
        if (!(lifetime > 0) || !Double.isFinite(now)) { clear(); return; }
        double cutoff = now - lifetime;
        if (samples.isEmpty()) return;
        if (samples.get(samples.size() - 1).time() < cutoff) { clear(); return; }
        PlayerMomsKnifeMotion.Blade boundary = sample(cutoff);
        int removed = 0;
        while (removed < samples.size() && samples.get(removed).time() < cutoff) removed++;
        removePrefix(removed);
        if (boundary != null && (samples.isEmpty() || samples.get(0).time() > cutoff)) {
            samples.add(0, new Sample(cutoff, boundary, false));
            segments.add(0, new Segment(boundary, boundary));
            if (samples.size() > 1) segments.set(1, new Segment(boundary, samples.get(1).blade()));
            boundsDirty = true;
        }
    }

    private void removePrefix(int count) {
        if (count == 0) return;
        samples.subList(0, count).clear();
        segments.subList(0, count).clear();
        if (!samples.isEmpty()) segments.set(0, new Segment(samples.get(0).blade(), samples.get(0).blade()));
        boundsDirty = true;
    }

    @Nullable
    public AABB bounds() {
        if (boundsDirty) {
            bounds = null;
            for (Segment segment : segments) bounds = bounds == null ? segment.bounds : bounds.minmax(segment.bounds);
            boundsDirty = false;
        }
        return bounds;
    }

    public boolean contacts(AABB target) {
        AABB search = bounds();
        if (search == null || !search.intersects(target)) return false;
        for (Segment segment : segments) if (segment.contacts(target)) return true;
        return false;
    }

    @Nullable
    public PlayerMomsKnifeMotion.Blade sample(double time) {
        if (samples.isEmpty() || time < samples.get(0).time()
                || time > samples.get(samples.size() - 1).time()) return null;
        for (int i = 1; i < samples.size(); i++) {
            Sample to = samples.get(i), from = samples.get(i - 1);
            if (time > to.time()) continue;
            if (time == from.time()) return from.blade();
            if (time == to.time()) return to.blade();
            if (!to.connected()) return null;
            double fraction = (time - from.time()) / (to.time() - from.time());
            Vec3 before = PlayerMomsKnifeMotion.unit(from.blade().tip().subtract(from.blade().base()));
            Vec3 after = PlayerMomsKnifeMotion.unit(to.blade().tip().subtract(to.blade().base()));
            boolean reversal = before.dot(after) < -0.999
                    && from.blade().anchor().distanceToSqr(to.blade().anchor()) < 1e-8;
            Vec3 direction = reversal ? (fraction < 1 ? before : after)
                    : PlayerMomsKnifeMotion.interpolateDirection(before, after, fraction);
            return PlayerMomsKnifeMotion.blade(from.blade().anchor().lerp(to.blade().anchor(), fraction), direction);
        }
        return samples.get(samples.size() - 1).blade();
    }

    static final class Path {
        final AABB bounds;
        private final List<Segment> segments = new ArrayList<>();

        Path(List<PlayerMomsKnifeMotion.Blade> blades) {
            AABB aggregate = null;
            for (int i = 0; i < blades.size(); i++) {
                Segment segment = new Segment(blades.get(Math.max(0, i - 1)), blades.get(i));
                segments.add(segment);
                aggregate = aggregate == null ? segment.bounds : aggregate.minmax(segment.bounds);
            }
            if (aggregate == null) throw new IllegalArgumentException("A knife contact path needs a blade");
            bounds = aggregate;
        }

        boolean contacts(AABB target) {
            if (!bounds.intersects(target)) return false;
            for (Segment segment : segments) if (segment.contacts(target)) return true;
            return false;
        }
    }

    private static final class Segment {
        final PlayerMomsKnifeMotion.Blade from, to;
        final AABB bounds;

        Segment(PlayerMomsKnifeMotion.Blade from, PlayerMomsKnifeMotion.Blade to) {
            this.from = from;
            this.to = to;
            bounds = from.bounds().minmax(to.bounds()).inflate(PlayerMomsKnifeMotion.ENTITY_HIT_PADDING);
        }

        boolean contacts(AABB target) {
            return bounds.intersects(target) && PlayerMomsKnifeMotion.contacts(from, to, target,
                    PlayerMomsKnifeMotion.ENTITY_HIT_PADDING);
        }
    }
}
