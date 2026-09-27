package net.luojiuoscar.isaac_disaster.registries.trajectory;

/** Numeric priorities assigned to this mod's built-in primary trajectory modules. */
public enum TrajectoryModulePriority {
    TINY_PLANET_LASER(100),
    MY_REFLECTION_LASER(50),
    DEFAULT(0),
    MY_REFLECTION_BULLET(-50),
    GRAVITY(-100);

    private final int priority;

    TrajectoryModulePriority(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
