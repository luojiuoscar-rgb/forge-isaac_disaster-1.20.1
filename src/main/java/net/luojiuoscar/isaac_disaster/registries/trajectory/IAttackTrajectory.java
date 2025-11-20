package net.luojiuoscar.isaac_disaster.registries.trajectory;


@FunctionalInterface
public interface IAttackTrajectory {
    TrajectoryResult getResult(TrajectoryContext ctx);
}
