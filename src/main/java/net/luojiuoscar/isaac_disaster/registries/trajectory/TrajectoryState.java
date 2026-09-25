package net.luojiuoscar.isaac_disaster.registries.trajectory;

/** Mutable state owned by one projectile and one registered trajectory module. */
public interface TrajectoryState {
    TrajectoryState copy();

    /** Called when homing/control temporarily takes over the projectile. */
    default void suspend() {}

    /** Called when trajectory evaluation resumes after a redirect. */
    default void resume() {}

    /** Rebinds cached launch geometry for a newly spawned split child. */
    default void rebase() {}
}
