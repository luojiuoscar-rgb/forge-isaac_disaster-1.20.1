package net.luojiuoscar.isaac_disaster.registries.trajectory;

import java.util.List;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;

public abstract class TrajectoryModule {
    public enum Role {
        PRIMARY,
        OFFSET
    }

    public Role role() {
        return Role.PRIMARY;
    }

    /**
     * Ordering among primary motion modules. Higher values run first; this is a composition order,
     * not the order in which modules were attached.
     */
    public int priority() {
        return 0;
    }

    /**
     * How this module's reported range cost combines with earlier primary costs. MINIMUM keeps the
     * least cost in the composed step; REPLACE makes this module's cost authoritative.
     */
    public enum RangeCostPolicy {
        MINIMUM,
        REPLACE
    }

    public RangeCostPolicy rangeCostPolicy() {
        return RangeCostPolicy.MINIMUM;
    }

    /**
     * Whether this module temporarily prevents later primary modules from taking over while its
     * current phase is active.
     */
    public boolean blocksFollowingPrimary(TrajectoryRuntimeState state, int amplifier) {
        return false;
    }

    public boolean freeDistanceCoveredByPrimary(TrajectoryRuntimeState state, boolean hasPrimary) {
        return false;
    }

    public boolean appliesTo(IBulletObject bullet) {
        return true;
    }

    /** Evaluates a step; mutable memory is confined to the projectile context. */
    public abstract TrajectoryMotion apply(TrajectoryContext ctx);

    /** Captures immutable launch data before the first network snapshot. */
    public void initialize(TrajectoryContext context) {
    }

    /** Upper bound on travel that can occur without consuming laser range. */
    public double maximumFreeDistance(int amplifier) {
        return 0;
    }

    public double maximumFreeDistance(int amplifier, List<TrajectorySpec> specs) {
        return maximumFreeDistance(amplifier);
    }

    public double maximumFreeDistance(int amplifier, List<TrajectorySpec> specs, double range) {
        return maximumFreeDistance(amplifier, specs);
    }

    protected final double stepDistance(TrajectoryContext context) {
        return context.baseVelocity.length() * Math.max(0, context.deltaTicks);
    }
}
