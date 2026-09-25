package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

/**
 * Stateless registry definition for one trajectory. Mutable per-projectile data belongs to the
 * state instance supplied by {@link TrajectoryRuntime}.
 */
public abstract class TrajectoryModule<S extends TrajectoryState> {
    public enum Role {
        PRIMARY,
        OFFSET
    }

    public Role role() {
        return Role.PRIMARY;
    }

    /** Higher values run first. */
    public int priority() {
        return 0;
    }

    public enum RangeCostPolicy {
        MINIMUM,
        REPLACE
    }

    public RangeCostPolicy rangeCostPolicy() {
        return RangeCostPolicy.MINIMUM;
    }

    public abstract S createState();

    protected abstract Class<S> stateClass();

    public abstract void writeState(FriendlyByteBuf buffer, S state);

    public abstract S readState(FriendlyByteBuf buffer);

    public final S castState(TrajectoryState state) {
        if (state == null) return createState();
        if (!stateClass().isInstance(state))
            throw new IllegalStateException(
                    "Trajectory state " + state.getClass().getName()
                            + " does not belong to " + getClass().getName());
        return stateClass().cast(state);
    }

    public final void writeStateUnchecked(FriendlyByteBuf buffer, TrajectoryState state) {
        writeState(buffer, castState(state));
    }

    protected final S state(TrajectoryContext context) {
        return castState(context.runtimeState);
    }

    public boolean blocksFollowingPrimary(TrajectoryState state, int amplifier) {
        return blocksFollowingPrimaryTyped(castState(state), amplifier);
    }

    protected boolean blocksFollowingPrimaryTyped(S state, int amplifier) {
        return false;
    }

    public boolean freeDistanceCoveredByPrimary(TrajectoryState state, boolean hasPrimary) {
        return freeDistanceCoveredByPrimaryTyped(castState(state), hasPrimary);
    }

    protected boolean freeDistanceCoveredByPrimaryTyped(S state, boolean hasPrimary) {
        return false;
    }

    /** Progress already consumed by this module's free-distance phase. */
    public double progressDistance(TrajectoryState state) {
        return 0;
    }

    /** Read-only diagnostic phase exposed without knowing a concrete state type. */
    public double telemetryPhase(TrajectoryState state) {
        return progressDistance(state);
    }

    public boolean appliesTo(IBulletObject bullet) {
        return true;
    }

    public final TrajectoryMotion apply(TrajectoryContext context) {
        return applyTyped(context, state(context));
    }

    protected abstract TrajectoryMotion applyTyped(TrajectoryContext context, S state);

    public final void initialize(TrajectoryContext context) {
        initializeTyped(context, state(context));
    }

    protected void initializeTyped(TrajectoryContext context, S state) {}

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
