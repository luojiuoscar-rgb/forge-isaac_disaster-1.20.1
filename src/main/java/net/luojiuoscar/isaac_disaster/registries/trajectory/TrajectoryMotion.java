package net.luojiuoscar.isaac_disaster.registries.trajectory;

import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Immutable result of one trajectory evaluation step. */
public record TrajectoryMotion(
    Vec3 desiredPosition,
    Vec3 desiredVelocity,
    Vec3 desiredAcceleration,
    double progressRate,
    CompositionMode compositionMode,
    double rangeCost,
    List<Vec3> path,
    List<Double> pathCosts,
    Status status) {

    public TrajectoryMotion(
        Vec3 desiredPosition,
        Vec3 desiredVelocity,
        Vec3 desiredAcceleration,
        double progressRate,
        CompositionMode compositionMode,
        double rangeCost,
        List<Vec3> path,
        List<Double> pathCosts) {
        this(
            desiredPosition,
            desiredVelocity,
            desiredAcceleration,
            progressRate,
            compositionMode,
            rangeCost,
            path,
            pathCosts,
            Status.OK);
    }

    public TrajectoryMotion(
        Vec3 position,
        Vec3 velocity,
        Vec3 acceleration,
        double progress,
        CompositionMode composition,
        double rangeCost) {
        this(
            position,
            velocity,
            acceleration,
            progress,
            composition,
            rangeCost,
            List.of(),
            List.of());
    }

    public TrajectoryMotion(
        Vec3 position,
        Vec3 velocity,
        Vec3 acceleration,
        double progress,
        CompositionMode composition,
        double rangeCost,
        List<Vec3> path) {
        this(
            position,
            velocity,
            acceleration,
            progress,
            composition,
            rangeCost,
            path,
            List.of());
    }

    public TrajectoryMotion(
        Vec3 position,
        Vec3 velocity,
        Vec3 acceleration,
        double progress,
        CompositionMode composition) {
        this(position, velocity, acceleration, progress, composition, Double.NaN);
    }

    public TrajectoryMotion(
        Vec3 position, Vec3 velocity, Vec3 acceleration, double progress) {
        this(position, velocity, acceleration, progress, CompositionMode.ABSOLUTE);
    }

    public enum Status {
        OK,
        WORK_LIMIT
    }

    /** Describes whether a motion is an absolute path or an increment from its input. */
    public enum CompositionMode {
        ABSOLUTE,
        RELATIVE,
        PRIMARY
    }

    /** NaN preserves backend accounting; zero is explicitly free. */
    public double chargedDistance(double fallback) {
        return Double.isFinite(rangeCost) ? Math.max(0, rangeCost) : fallback;
    }

    public static TrajectoryMotion ofPosition(Vec3 position, Vec3 velocity, double progressRate) {
        return new TrajectoryMotion(
            position, velocity, Vec3.ZERO, progressRate, CompositionMode.ABSOLUTE);
    }

    public static TrajectoryMotion workLimited(Vec3 position, Vec3 velocity) {
        return new TrajectoryMotion(
            position,
            velocity,
            Vec3.ZERO,
            0.0D,
            CompositionMode.ABSOLUTE,
            0.0D,
            List.of(),
            List.of(),
            Status.WORK_LIMIT);
    }

    public TrajectoryMotion(
        Vec3 desiredPosition,
        Vec3 desiredVelocity,
        Vec3 desiredAcceleration,
        double progressRate,
        CompositionMode compositionMode,
        double rangeCost,
        List<Vec3> path,
        List<Double> pathCosts,
        Status status) {
        this.desiredPosition = finiteOrNull(desiredPosition);
        this.desiredVelocity = finiteOrNull(desiredVelocity);
        this.desiredAcceleration = finiteOrZero(desiredAcceleration);
        this.progressRate =
            Double.isFinite(progressRate) && progressRate >= 0.0D ? progressRate : 0.0D;
        this.compositionMode =
            compositionMode == null ? CompositionMode.ABSOLUTE : compositionMode;
        this.rangeCost = rangeCost;
        this.path = path == null ? List.of() : List.copyOf(path);
        this.pathCosts = pathCosts == null ? List.of() : List.copyOf(pathCosts);
        if (!this.pathCosts.isEmpty() && this.pathCosts.size() != this.path.size()) {
            throw new IllegalArgumentException("path cost count");
        }
        this.status = status == null ? Status.OK : status;
    }

    public static TrajectoryMotion relative(
        Vec3 position, Vec3 velocity, Vec3 acceleration, double progressRate) {
        return new TrajectoryMotion(
            position, velocity, acceleration, progressRate, CompositionMode.RELATIVE);
    }

    private static Vec3 finiteOrNull(Vec3 value) {
        return value != null
                && Double.isFinite(value.x)
                && Double.isFinite(value.y)
                && Double.isFinite(value.z)
            ? value
            : null;
    }

    private static Vec3 finiteOrZero(Vec3 value) {
        Vec3 result = finiteOrNull(value);
        return result == null ? Vec3.ZERO : result;
    }
}
