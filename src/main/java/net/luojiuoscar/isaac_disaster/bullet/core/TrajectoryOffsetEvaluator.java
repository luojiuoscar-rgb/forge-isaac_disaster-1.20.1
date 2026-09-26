package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.*;
import java.util.function.Function;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Adds shape offsets to one primary solution while preserving exact collision samples. */
final class TrajectoryOffsetEvaluator {
    static final int MAX_SAMPLES_PER_STEP = 4096;

    private record Offset(
        OffsetTrajectoryModule module, TrajectoryState state, int amplifier) {}

    private TrajectoryOffsetEvaluator() {
    }

    static TrajectoryMotion compose(
        IBulletObject bullet,
        List<TrajectorySpec> specs,
        Map<ResourceLocation, TrajectoryState> states,
        TrajectoryKinematics k,
        TrajectoryMotion main,
        boolean hasPrimary,
        double speed,
        double dt,
        Function<ResourceLocation, TrajectoryModule<?>> modules) {
        List<Offset> offsets = new ArrayList<>(specs.size());
        for (TrajectorySpec spec : specs) {
            offsets.add(
                new Offset(
                    (OffsetTrajectoryModule) modules.apply(spec.id()),
                    states.computeIfAbsent(spec.id(), id -> modules.apply(id).createState()),
                    spec.amplifier()));
        }

        Vec3 start = k.position();
        List<Vec3> primaryPath = main.path().isEmpty() ? List.of(main.desiredPosition()) : main.path();
        double sourceLength = 0.0D;
        Vec3 previous = start;
        for (Vec3 point : primaryPath) {
            sourceLength += point.distanceTo(previous);
            previous = point;
        }
        double sourceCost = main.chargedDistance(sourceLength);
        double remaining = Math.max(0.0D, bullet.getRange() - bullet.getTraveled());
        double amplitude =
            offsets.stream().mapToDouble(o -> o.module.amplitude(o.amplifier) * 2.0D).sum();
        double resolution = 0.05D / (1.0D + amplitude);
        double phaseDistance =
            offsets.stream()
                .mapToDouble(o -> o.module.maxPhaseAdvance())
                .min()
                .orElse(Double.POSITIVE_INFINITY);
        double safeDt = Math.max(0.0D, dt);
        double maxRate = Math.max(speed, safeDt > 1.0E-12D ? sourceLength / safeDt : 0.0D);
        long estimated =
            primaryPath.size()
                + 32L
                + finiteCeil(maxRate * safeDt / Math.max(1.0E-12D, resolution))
                + finiteCeil(sourceLength / Math.max(1.0E-12D, phaseDistance));
        if (estimated > MAX_SAMPLES_PER_STEP) {
            return TrajectoryMotion.workLimited(bullet.getPosition(), bullet.getVelocity());
        }

        List<Vec3> path =
            new ArrayList<>((int) Math.min(MAX_SAMPLES_PER_STEP, Math.max(1L, estimated)));
        List<Double> costs = new ArrayList<>(path.size());
        Vec3 actual = bullet.getPosition();
        Vec3 tangent = bullet.getVelocity();
        double time = 0.0D;
        double charged = 0.0D;
        double moved = 0.0D;
        int samples = 0;
        while (time < dt - 1.0E-12D && charged < remaining) {
            if (++samples > MAX_SAMPLES_PER_STEP) {
                return TrajectoryMotion.workLimited(actual, tangent);
            }
            int segment =
                Math.min(
                    primaryPath.size() - 1, (int) Math.floor(time / dt * primaryPath.size() + 1.0E-10D));
            double segmentEnd = dt * (segment + 1) / primaryPath.size();
            Vec3 a = segment == 0 ? start : primaryPath.get(segment - 1);
            Vec3 b = primaryPath.get(segment);
            Vec3 sourceVelocity = b.subtract(a).scale(primaryPath.size() / dt);
            boolean pauseDefault =
                offsets.stream().anyMatch(o -> o.module.pausesDefaultMotion(o.state, hasPrimary));
            Vec3 primaryVelocity =
                hasPrimary ? sourceVelocity : pauseDefault ? Vec3.ZERO : k.forward().scale(speed);
            double primaryRate = primaryVelocity.length();
            double step = Math.min(dt - time, segmentEnd - time);
            step = Math.min(step, resolution / Math.max(1.0E-12D, Math.max(speed, primaryRate)));
            if (primaryRate > 1.0E-12D) step = Math.min(step, phaseDistance / primaryRate);
            for (Offset offset : offsets) {
                double rate = offset.module.boundaryAdvanceRate(offset.state, primaryRate, speed);
                if (rate > 1.0E-12D) {
                    step = Math.min(step, offset.module.distanceToBoundary(offset.state) / rate);
                }
            }
            if (step <= 1.0E-12D) {
                if (segmentEnd - time <= 1.0E-12D) {
                    time = segmentEnd;
                    continue;
                }
                boolean changed = false;
                for (Offset offset : offsets) {
                    int stage = offset.module.stage(offset.state);
                    offset.module.advance(offset.state, 0.0D, 0.0D);
                    changed |= stage != offset.module.stage(offset.state);
                }
                if (changed) continue;
                break;
            }

            double distance = primaryRate * step;
            double cost =
                hasPrimary
                    ? !main.pathCosts().isEmpty()
                        ? main.pathCosts().get(segment) * step * primaryPath.size() / dt
                        : sourceLength > 1.0E-12D ? sourceCost * distance / sourceLength : 0.0D
                    : distance;
            if (cost > remaining - charged) {
                double ratio = (remaining - charged) / cost;
                step *= ratio;
                distance *= ratio;
                cost = remaining - charged;
            }

            k.orient(primaryVelocity);
            k.position(k.position().add(primaryVelocity.scale(step)));
            k.advanceDistance(distance);
            k.recovery(Math.min(2.0D, k.recovery() + distance));
            for (Offset offset : offsets) {
                offset.module.advance(offset.state, distance, speed * step);
                offset.state.resume();
            }
            k.offset(sum(offsets, k).scale(TrajectoryFrame.smootherstep(k.recovery() / 2.0D)));
            Vec3 next = k.position().add(k.offset());
            tangent = next.subtract(actual).scale(1.0D / step);
            actual = next;
            path.add(actual);
            costs.add(cost);
            moved += distance;
            charged += cost;
            time += step;
        }

        k.velocity(main.desiredVelocity() == null ? k.velocity() : main.desiredVelocity());
        k.acceleration(main.desiredAcceleration());
        k.advanceClock(hasPrimary ? main.progressRate() * dt : moved);
        double progress = dt > 0.0D && time > 0.0D ? Math.max(speed * time, moved) / dt : 0.0D;
        return new TrajectoryMotion(
            actual,
            tangent,
            k.acceleration(),
            progress,
            TrajectoryMotion.CompositionMode.ABSOLUTE,
            charged,
            path,
            costs);
    }

    private static long finiteCeil(double value) {
        if (!Double.isFinite(value) || value >= MAX_SAMPLES_PER_STEP) return MAX_SAMPLES_PER_STEP + 1L;
        return Math.max(0L, (long) Math.ceil(value));
    }

    private static Vec3 sum(List<Offset> offsets, TrajectoryKinematics frame) {
        Vec3 result = Vec3.ZERO;
        for (Offset offset : offsets) {
            result = result.add(offset.module.offset(frame, offset.state, offset.amplifier));
        }
        return result;
    }
}
