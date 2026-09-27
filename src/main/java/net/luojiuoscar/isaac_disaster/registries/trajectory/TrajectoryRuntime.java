package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One projectile's trajectory memory. Configuration is immutable; mutable state is never shared.
 */
public final class TrajectoryRuntime {
    private Vec3 origin;
    private Vec3 launchDirection;

    /** Current primary direction; launchDirection remains immutable geometry input. */
    private Vec3 mainDirection;

    private Vec3 anchor = Vec3.ZERO;
    private int anchorEntityId = -1;
    private List<TrajectorySpec> specs;

    /** Uncharged trajectory clock, distinct from range consumption and primary path length. */
    private double distance;

    private Vec3 compositionOffset = Vec3.ZERO;
    private Vec3 compositionVelocityOffset = Vec3.ZERO;
    private final Map<ResourceLocation, TrajectoryState> states = new LinkedHashMap<>();
    private TrajectoryKinematics kinematics;
    private boolean suspended;
    private Snapshot stepCheckpoint;
    private Vec3 pendingRedirectPosition;
    private Vec3 pendingRedirectDirection;

    public TrajectoryRuntime(Vec3 origin, Vec3 launchDirection, List<TrajectorySpec> specs) {
        this.origin = finiteOrZero(origin);
        Vec3 axis = finiteOrZero(launchDirection);
        this.launchDirection = axis.lengthSqr() > 1e-12 ? axis.normalize() : new Vec3(1, 0, 0);
        this.mainDirection = this.launchDirection;
        this.specs = List.copyOf(specs);
    }

    /**
     * New shots use the final spawn frame; children inherit progress, never the parent's launch
     * frame.
     */
    public static TrajectoryRuntime forSpawn(
            Vec3 origin,
            Vec3 direction,
            List<TrajectorySpec> specs,
            @Nullable Object shooter,
            @Nullable Snapshot inherited) {
        TrajectoryRuntime result = new TrajectoryRuntime(origin, direction, specs);
        if (inherited != null) {
            TrajectoryRuntime parent = inherited.saved;
            result.anchor = parent.anchor;
            result.distance = parent.distance;
            for (TrajectorySpec spec : specs) {
                TrajectoryState state = parent.states.get(spec.id());
                if (state != null) {
                    TrajectoryState copy = state.copy();
                    copy.rebase();
                    result.states.put(spec.id(), copy);
                }
            }
            if (parent.kinematics != null) {
                result.kinematics = parent.kinematics.copy();
                result.kinematics.position(result.origin);
                result.kinematics.velocity(finiteOrZero(direction));
                result.kinematics.offset(Vec3.ZERO);
                result.kinematics.initializeLaunchBasis(result.launchDirection);
            }
            result.suspend();
        }
        if (shooter instanceof Entity entity) result.updateAnchor(entity);
        return result;
    }

    public Vec3 origin() {
        return origin;
    }

    public Vec3 launchDirection() {
        return launchDirection;
    }

    public Vec3 mainDirection() {
        return mainDirection;
    }

    /** Starts a transactional evaluator step. Nested starts are intentionally ignored. */
    public void beginStep() {
        if (stepCheckpoint == null) stepCheckpoint = snapshot();
    }

    /** Commits all phase, kinematics and distance changes made by the current step. */
    public void commitStep() {
        stepCheckpoint = null;
        pendingRedirectPosition = null;
        pendingRedirectDirection = null;
    }

    /** Restores the last checkpoint, then reapplies a redirect raised by a collision callback. */
    public void rollbackStep() {
        if (stepCheckpoint == null) return;
        Snapshot checkpoint = stepCheckpoint;
        Vec3 redirectPosition = pendingRedirectPosition;
        Vec3 redirectDirection = pendingRedirectDirection;
        restore(checkpoint);
        stepCheckpoint = null;
        pendingRedirectPosition = null;
        pendingRedirectDirection = null;
        if (redirectDirection != null) redirect(redirectPosition, redirectDirection);
        else suspend();
    }

    /** Changes the primary axis without resetting module phase or launch geometry. */
    public void redirect(Vec3 position, Vec3 direction) {
        Vec3 safe = finiteOrZero(direction);
        if (safe.lengthSqr() < 1e-12) return;
        mainDirection = safe.normalize();
        compositionOffset = Vec3.ZERO;
        compositionVelocityOffset = Vec3.ZERO;
        if (kinematics != null) {
            kinematics.position(finiteOrZero(position));
            kinematics.velocity(safe);
            kinematics.offset(Vec3.ZERO);
            kinematics.initialized(false);
        }
        suspended = true;
        for (TrajectoryState state : states.values()) state.suspend();
        if (stepCheckpoint != null) {
            pendingRedirectPosition = finiteOrZero(position);
            pendingRedirectDirection = safe;
        }
    }

    public Vec3 anchor() {
        return anchor;
    }

    public int anchorEntityId() {
        return anchorEntityId;
    }

    public List<TrajectorySpec> specs() {
        return specs;
    }

    public double distance() {
        return distance;
    }

    public void distance(double value) {
        distance = Double.isFinite(value) ? Math.max(0, value) : 0;
    }

    public void advance(double amount) {
        if (Double.isFinite(amount) && amount > 0) distance += amount;
    }

    public Vec3 compositionOffset() {
        return compositionOffset;
    }

    public void compositionOffset(Vec3 value) {
        compositionOffset = finiteOrZero(value);
    }

    public Vec3 compositionVelocityOffset() {
        return compositionVelocityOffset;
    }

    public void compositionVelocityOffset(Vec3 value) {
        compositionVelocityOffset = finiteOrZero(value);
    }

    public Map<ResourceLocation, TrajectoryState> states() {
        return states;
    }

    public TrajectoryState state(ResourceLocation id) {
        return states.get(id);
    }

    public TrajectoryState ensureState(ResourceLocation id, TrajectoryModule<?> module) {
        return states.computeIfAbsent(id, ignored -> module.createState());
    }

    public void putState(ResourceLocation id, TrajectoryState state) {
        states.put(id, state);
    }

    public boolean hasKinematics() {
        return kinematics != null;
    }

    public TrajectoryKinematics kinematics() {
        if (kinematics == null) kinematics = new TrajectoryKinematics();
        return kinematics;
    }

    public boolean suspended() {
        return suspended;
    }

    public void resume() {
        suspended = false;
    }

    public void suspend() {
        suspended = true;
        states.values().forEach(TrajectoryState::suspend);
    }

    public void anchor(Vec3 value) {
        anchor = finiteOrZero(value);
    }

    /**
     * Both prediction and authority resolve the same shooter, rather than assuming shooter ==
     * owner.
     */
    public void updateAnchor(@Nullable Entity shooter) {
        if (shooter == null) return;
        anchorEntityId = shooter.getId();
        anchor(shooter.position().add(0, shooter.getBbHeight() * 0.6, 0));
    }

    public Snapshot snapshot() {
        return new Snapshot(copy());
    }

    /** Restore all counters together; mixing a new phase with an old clock is invalid. */
    public void restore(Snapshot snapshot) {
        TrajectoryRuntime source = snapshot.saved;
        origin = source.origin;
        launchDirection = source.launchDirection;
        mainDirection = source.mainDirection;
        anchor = source.anchor;
        anchorEntityId = source.anchorEntityId;
        specs = source.specs;
        distance = source.distance;
        compositionOffset = source.compositionOffset;
        compositionVelocityOffset = source.compositionVelocityOffset;
        states.clear();
        source.states.forEach((id, state) -> states.put(id, state.copy()));
        kinematics = source.kinematics == null ? null : source.kinematics.copy();
        suspended = source.suspended;
        stepCheckpoint = null;
        pendingRedirectPosition = null;
        pendingRedirectDirection = null;
    }

    private TrajectoryRuntime copy() {
        TrajectoryRuntime copy = new TrajectoryRuntime(origin, launchDirection, specs);
        copy.launchDirection = launchDirection;
        copy.mainDirection = mainDirection;
        copy.anchor = anchor;
        copy.anchorEntityId = anchorEntityId;
        copy.distance = distance;
        copy.compositionOffset = compositionOffset;
        copy.compositionVelocityOffset = compositionVelocityOffset;
        states.forEach((id, state) -> copy.states.put(id, state.copy()));
        copy.kinematics = kinematics == null ? null : kinematics.copy();
        copy.suspended = suspended;
        return copy;
    }

    /** Immutable transport/inheritance value. Reading it always creates an independent runtime. */
    public static final class Snapshot {
        private final TrajectoryRuntime saved;

        private Snapshot(TrajectoryRuntime ownedCopy) {
            saved = ownedCopy;
        }

        public TrajectoryRuntime restore() {
            return saved.copy();
        }

        public void write(FriendlyByteBuf buf) {
            writeVec(buf, saved.origin);
            writeVec(buf, saved.launchDirection);
            writeVec(buf, saved.mainDirection);
            writeVec(buf, saved.anchor);
            buf.writeInt(saved.anchorEntityId);
            buf.writeCollection(
                    saved.specs,
                    (b, spec) -> {
                        b.writeResourceLocation(spec.id());
                        b.writeVarInt(spec.amplifier());
                    });
            buf.writeDouble(saved.distance);
            writeVec(buf, saved.compositionOffset);
            writeVec(buf, saved.compositionVelocityOffset);
            buf.writeVarInt(saved.states.size());
            for (Map.Entry<ResourceLocation, TrajectoryState> entry : saved.states.entrySet()) {
                TrajectoryModule<?> module = ModTrajectoryModules.resolve(entry.getKey());
                if (module == null) {
                    throw new IllegalStateException(
                            "Unknown trajectory module state " + entry.getKey());
                }
                buf.writeResourceLocation(entry.getKey());
                module.writeStateUnchecked(buf, entry.getValue());
            }
            buf.writeBoolean(saved.kinematics != null);
            if (saved.kinematics != null) saved.kinematics.write(buf);
            buf.writeBoolean(saved.suspended);
        }

        public static Snapshot read(FriendlyByteBuf buf) {
            Vec3 origin = readVec(buf),
                    axis = readVec(buf),
                    main = readVec(buf),
                    anchor = readVec(buf);
            int anchorId = buf.readInt();
            List<TrajectorySpec> specs =
                    buf.readList(b -> new TrajectorySpec(b.readResourceLocation(), b.readVarInt()));
            TrajectoryRuntime runtime = new TrajectoryRuntime(origin, axis, specs);
            runtime.mainDirection =
                    runtime.finiteOrZero(main).lengthSqr() > 1e-12
                            ? runtime.finiteOrZero(main).normalize()
                            : runtime.launchDirection;
            runtime.anchor(anchor);
            runtime.anchorEntityId = anchorId;
            runtime.distance(buf.readDouble());
            runtime.compositionOffset(readVec(buf));
            runtime.compositionVelocityOffset(readVec(buf));
            int stateCount = buf.readVarInt();
            for (int i = 0; i < stateCount; i++) {
                ResourceLocation id = buf.readResourceLocation();
                TrajectoryModule<?> module = ModTrajectoryModules.resolve(id);
                if (module == null) {
                    throw new IllegalStateException("Unknown trajectory module state " + id);
                }
                runtime.states.put(id, module.readState(buf));
            }
            if (buf.readBoolean()) runtime.kinematics = TrajectoryKinematics.read(buf);
            runtime.suspended = buf.readBoolean();
            return new Snapshot(runtime);
        }
    }

    private static Vec3 finiteOrZero(Vec3 value) {
        return value != null
                        && Double.isFinite(value.x)
                        && Double.isFinite(value.y)
                        && Double.isFinite(value.z)
                ? value
                : Vec3.ZERO;
    }

    private static Vec3 readVec(FriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private static void writeVec(FriendlyByteBuf buf, Vec3 value) {
        buf.writeDouble(value.x);
        buf.writeDouble(value.y);
        buf.writeDouble(value.z);
    }

}
