package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;

/** Per-bullet adapter between base motion and the trajectory runtime. */
public final class BulletTrajectoryState {
    private final TrajectoryRuntime runtime;
    private TrajectoryMotion currentMotion;
    private double progressRate;
    private double lastInputDistance;
    private int evaluationCount;

    BulletTrajectoryState(TrajectoryRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    public TrajectoryRuntime runtime() {
        return runtime;
    }

    public void suspendForPhysics() {
        currentMotion = null;
        progressRate = 0.0D;
        runtime.suspend();
    }

    public void advanceRuntime(double progress, double deltaTicks) {
        progressRate = Math.max(0.0D, progress);
        runtime.advance(progressRate * Math.max(0.0D, deltaTicks));
    }

    public void captureMotion(TrajectoryMotion motion) {
        currentMotion = motion;
    }

    public TrajectoryMotion currentMotion() {
        return currentMotion;
    }

    public double progressRate() {
        return progressRate;
    }

    public double lastInputDistance() {
        return lastInputDistance;
    }

    public int evaluationCount() {
        return evaluationCount;
    }

    public void markEvaluation(double inputDistance) {
        lastInputDistance = Math.max(0.0D, inputDistance);
        evaluationCount++;
    }

    public double truncateMovementDistance(double traveled, double fullDistance, double acceptedDistance) {
        if (currentMotion != null && Double.isFinite(currentMotion.rangeCost())) return traveled;
        if (!Double.isFinite(fullDistance) || !Double.isFinite(acceptedDistance)) return traveled;
        return Math.max(
            0.0D,
            traveled
                - Math.max(0.0D, fullDistance)
                + Math.max(0.0D, Math.min(fullDistance, acceptedDistance)));
    }

    public double retainTrajectoryFraction(double traveled, Vec3 start, double fraction) {
        if (currentMotion == null || !Double.isFinite(currentMotion.rangeCost())) return traveled;
        double bounded = Math.max(0.0D, Math.min(1.0D, fraction));
        double retained = currentMotion.rangeCost() * bounded;
        if (!currentMotion.pathCosts().isEmpty()) {
            double cursor = bounded * currentMotion.pathCosts().size();
            int complete = (int) cursor;
            retained = 0.0D;
            for (int i = 0; i < complete; i++) retained += currentMotion.pathCosts().get(i);
            if (complete < currentMotion.pathCosts().size()) {
                retained += currentMotion.pathCosts().get(complete) * (cursor - complete);
            }
        }
        if (currentMotion.pathCosts().isEmpty() && !currentMotion.path().isEmpty()) {
            double cursor = bounded * currentMotion.path().size();
            double total = 0, accepted = 0;
            Vec3 previous = start;
            for (int i = 0; i < currentMotion.path().size(); i++) {
                Vec3 point = currentMotion.path().get(i);
                double length = previous.distanceTo(point);
                total += length;
                accepted += length * Math.max(0, Math.min(1, cursor - i));
                previous = point;
            }
            retained = total > 1.0E-12 ? currentMotion.rangeCost() * accepted / total : 0;
        }
        return Math.max(0.0D, traveled - currentMotion.rangeCost() + retained);
    }
    /** Recovers the sampled contact segment's velocity before attachment pauses the motion. */
    public Vec3 contactVelocity(Vec3 start, Vec3 contact, Vec3 fallback) {
        if (currentMotion == null || currentMotion.path().isEmpty() || currentMotion.desiredVelocity() == null
                || fallback.distanceToSqr(currentMotion.desiredVelocity()) > 1.0E-10) return fallback;
        int count = currentMotion.path().size();
        Vec3 lastStart = count > 1 ? currentMotion.path().get(count - 2) : start;
        Vec3 finalSegmentVelocity = currentMotion.path().get(count - 1).subtract(lastStart).scale(count);
        // Range clipping can shorten the final segment or omit substeps; use the authored velocity then.
        if (finalSegmentVelocity.distanceToSqr(fallback) > 1.0E-10) return fallback;
        Vec3 previous = start;
        for (Vec3 endpoint : currentMotion.path()) {
            Vec3 segment = endpoint.subtract(previous);
            double lengthSquared = segment.lengthSqr();
            double part = lengthSquared > 1.0E-12
                    ? Math.max(0, Math.min(1, contact.subtract(previous).dot(segment) / lengthSquared)) : 0;
            if (lengthSquared > 1.0E-12 && previous.add(segment.scale(part)).distanceToSqr(contact) < 1.0E-10)
                return segment.scale(currentMotion.path().size());
            previous = endpoint;
        }
        return fallback;
    }
}
