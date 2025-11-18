package net.luojiuoscar.isaac_disaster.registries.trajectory;


@FunctionalInterface
public interface AttackTrajectory {
    /**
     * Calculate trajectory result for this tick given context.
     * @param ctx context (distance, dir, owner, etc)
     * @return result describing position/velocity offset and optional rotations
     */
    TrajectoryResult getResult(TrajectoryContext ctx);
}
