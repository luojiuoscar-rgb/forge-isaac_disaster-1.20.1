package net.luojiuoscar.isaac_disaster.registries.trajectory;


@FunctionalInterface
public interface AttackTrajectory {
    TrajectoryResult getResult(TrajectoryContext ctx);
}
