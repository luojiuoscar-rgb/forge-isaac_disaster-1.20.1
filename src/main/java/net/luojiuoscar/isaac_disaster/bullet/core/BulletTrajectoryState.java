package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.Objects;
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

    public double retainTrajectoryFraction(double traveled, double fraction) {
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
        return Math.max(0.0D, traveled - currentMotion.rangeCost() + retained);
    }
}
