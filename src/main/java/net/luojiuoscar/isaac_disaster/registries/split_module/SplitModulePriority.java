package net.luojiuoscar.isaac_disaster.registries.split_module;

/** Numeric priorities assigned to concrete split modules. */
public enum SplitModulePriority {
    THE_PARASITE(0.0),
    COMPOUND_FRACTURE(0.0),
    CRICKETS_BODY(-1.0);

    private final double priority;

    SplitModulePriority(double priority) {
        this.priority = priority;
    }

    public double priority() {
        return priority;
    }
}
