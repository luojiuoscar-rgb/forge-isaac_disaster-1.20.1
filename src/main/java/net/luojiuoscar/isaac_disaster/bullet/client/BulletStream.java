package net.luojiuoscar.isaac_disaster.bullet.client;

import net.luojiuoscar.isaac_disaster.bullet.tracking.TrackingProfile;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletTrackingBatchS2CPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Client-side stream of predicted bullets, independent from Minecraft Entity tracking. */
public final class BulletStream {
    private static final int TRACKING_REPLAY_DELAY_TICKS = 2;
    private static final long TRACKING_HANDLE_GRACE_TICKS = 80L;
    private final Map<Long, BulletState> states = new HashMap<>();
    private final Map<Integer, Integer> latestGenerations = new HashMap<>();
    private final Map<Integer, Vec3> trackingPositions = new HashMap<>();
    private final Map<Integer, Vec3> previousTrackingPositions = new HashMap<>();
    private final Map<Integer, Integer> previousTrackingSampleTicks = new HashMap<>();
    private final Map<Integer, Integer> trackingSampleTicks = new HashMap<>();
    private final Map<Integer, Long> trackingHandleLastSeen = new HashMap<>();
    private final Map<Long, Integer> velocitySampleTicks = new HashMap<>();
    private final Map<Long, Vec3> authoritativeVelocities = new HashMap<>();
    private final Map<Long, Integer> authoritativeVelocityTicks = new HashMap<>();
    private final Map<Long, Integer> appliedVelocityCorrectionTicks = new HashMap<>();
    private long serverTick;
    private int epoch = Integer.MIN_VALUE;

    /** Installs one server-created state in the client prediction stream. */
    public BulletState spawn(int slot, int generation, int startTick, BulletState state) {
        Integer latestGeneration = latestGenerations.get(slot);
        if (latestGeneration != null && generation <= latestGeneration) return null;
        states.entrySet().removeIf(entry -> {
            if ((int) entry.getKey().longValue() != slot) return false;
            long oldIdentity = entry.getKey();
            removeIdentityMetadata(oldIdentity);
            return true;
        });
        state.assignSlot(slot, generation);
        states.put(key(slot, generation), state);
        latestGenerations.put(slot, generation);
        // Packet state is already a server-time snapshot. Client-local ticks cannot
        // be treated as server ticks because that fast-forwards delayed spawns.
        serverTick = Math.max(serverTick, startTick);
        return state;
    }

    /** Starts a new server stream and drops stale bullets when the server epoch changes. */
    public boolean acceptEpoch(int value) {
        // Zero is reserved by compatibility constructors for packets created before
        // stream epochs existed; it must never invalidate a live stream.
        if (value <= 0) return true;
        if (epoch == value) return true;
        // Network delivery is ordered per connection, but lifecycle packets can still
        // arrive after a world transition. A lower epoch belongs to an older stream
        // and must never clear or mutate the current world's prediction state.
        if (epoch != Integer.MIN_VALUE && value < epoch) return false;
        clearState();
        epoch = value;
        return true;
    }

    /** Clears all predicted state and returns the tick domain to its initial baseline. */
    public void clear() {
        clearState();
        epoch = Integer.MIN_VALUE;
    }

    /** Clears transient world state while retaining the last epoch for stale-packet rejection. */
    public void clearForWorldUnload() {
        clearState();
    }

    /** Clears predicted entries without changing the stream epoch. */
    private void clearState() {
        states.clear();
        latestGenerations.clear();
        trackingPositions.clear();
        previousTrackingPositions.clear();
        previousTrackingSampleTicks.clear();
        trackingSampleTicks.clear();
        trackingHandleLastSeen.clear();
        velocitySampleTicks.clear();
        authoritativeVelocities.clear();
        authoritativeVelocityTicks.clear();
        appliedVelocityCorrectionTicks.clear();
        serverTick = 0L;
    }

    /** Removes a state only when the slot generation is current. */
    public boolean despawn(int slot, int generation) {
        latestGenerations.merge(slot, generation, Math::max);
        long identity = key(slot, generation);
        BulletState removed = states.remove(identity);
        removeIdentityMetadata(identity);
        return removed != null;
    }
    /** Removes an entry using the packed generation/slot identity used by batch packets. */
    public boolean despawn(long identity) { return despawn((int) identity, (int) (identity >>> 32)); }

    /** Predicts one client tick and drops states that reached their lifetime. */
    public void tick() {
        serverTick++;
        cleanupTrackingHandles();
        List<Long> dead = new ArrayList<>();
        for (Map.Entry<Long, BulletState> entry : states.entrySet()) {
            BulletState state = entry.getValue();
            if (state.trackingHandle() != 0) {
                int handle = state.trackingHandle();
                Vec3 target = interpolatedTrackingPosition(handle, serverTick - TRACKING_REPLAY_DELAY_TICKS);
                if (target != null) {
                    TrackingProfile profile = TrackingProfile.forBullet(state);
                    Vec3 desired = profile.desiredVelocity(state, target, state.trackingUsesControl());
                    state.setDesiredVelocity(desired);
                    state.applySteering();
                }
            }
            long identity = entry.getKey();
            Integer authorityTick = authoritativeVelocityTicks.get(identity);
            Integer appliedTick = appliedVelocityCorrectionTicks.get(identity);
            if (authorityTick != null && (appliedTick == null || authorityTick > appliedTick)) {
                state.applyVelocityCorrection(authoritativeVelocities.get(identity));
                appliedVelocityCorrectionTicks.put(identity, authorityTick);
            }
            if (!state.tickPhysics()) dead.add(entry.getKey());
        }
        for (Long key : dead) {
            states.remove(key);
            removeIdentityMetadata(key);
        }
    }

    /** Applies authoritative velocity samples without resolving target entities on the client. */
    public void applyTrackingVelocity(int packetEpoch, int sampleTick,
                                      List<BulletTrackingBatchS2CPacket.TargetSample> targets,
                                      List<BulletTrackingBatchS2CPacket.VelocitySample> samples,
                                      List<BulletTrackingBatchS2CPacket.Assignment> assignments) {
        if (!acceptEpoch(packetEpoch)) return;
        for (BulletTrackingBatchS2CPacket.Assignment assignment : assignments) {
            BulletState state = get(assignment.slot(), assignment.generation());
            if (state == null) continue;
            state.setTrackingHandle(assignment.handle());
            state.setSteeringTarget(null, assignment.control());
            if (assignment.handle() != 0) trackingHandleLastSeen.put(assignment.handle(), serverTick);
        }
        for (BulletTrackingBatchS2CPacket.TargetSample target : targets) {
            if (hasHandleReference(target.handle())) installTrackingPosition(target.handle(), target.position(), sampleTick);
        }
        for (BulletTrackingBatchS2CPacket.VelocitySample sample : samples) {
            long identity = key(sample.slot(), sample.generation());
            Integer previousSample = velocitySampleTicks.get(identity);
            if (previousSample != null && sampleTick <= previousSample) continue;
            BulletState state = states.get(identity);
            if (state == null) continue;
            authoritativeVelocities.put(identity, sample.velocity());
            authoritativeVelocityTicks.put(identity, sampleTick);
            velocitySampleTicks.put(identity, sampleTick);
        }
    }

    /** Stores an ordered target sample while retaining the previous point for interpolation. */
    private void installTrackingPosition(int handle, Vec3 position, int sampleTick) {
        if (handle == 0 || position == null) return;
        trackingHandleLastSeen.put(handle, serverTick);
        Integer previousTick = trackingSampleTicks.get(handle);
        if (previousTick != null && sampleTick <= previousTick) return;
        Vec3 previous = trackingPositions.put(handle, position);
        if (previous != null && previousTick != null) {
            previousTrackingPositions.put(handle, previous);
            previousTrackingSampleTicks.put(handle, previousTick);
        } else {
            previousTrackingPositions.remove(handle);
            previousTrackingSampleTicks.remove(handle);
        }
        trackingSampleTicks.put(handle, sampleTick);
    }

    /** Returns the delayed target coordinate used by the local simulation tick. */
    private Vec3 interpolatedTrackingPosition(int handle, long simulationTick) {
        Vec3 latest = trackingPositions.get(handle);
        Integer latestTick = trackingSampleTicks.get(handle);
        Vec3 previous = previousTrackingPositions.get(handle);
        Integer previousTick = previousTrackingSampleTicks.get(handle);
        if (latest == null || latestTick == null) return null;
        if (previous == null || previousTick == null || latestTick <= previousTick) {
            return simulationTick >= latestTick ? latest : null;
        }
        double factor = (double) (simulationTick - previousTick) / (double) (latestTick - previousTick);
        if (factor <= 0.0D) return previous;
        if (factor >= 1.0D) return latest;
        return previous.lerp(latest, factor);
    }

    /** Removes target samples no longer referenced by any live client bullet. */
    private void cleanupTrackingHandles() {
        java.util.Set<Integer> referenced = new java.util.HashSet<>();
        for (BulletState state : states.values()) {
            if (state.trackingHandle() != 0) referenced.add(state.trackingHandle());
        }
        trackingHandleLastSeen.entrySet().removeIf(entry -> !referenced.contains(entry.getKey())
                && serverTick - entry.getValue() > TRACKING_HANDLE_GRACE_TICKS);
        trackingHandleLastSeen.keySet().removeIf(handle -> !trackingPositions.containsKey(handle));
        trackingPositions.keySet().removeIf(handle -> !trackingHandleLastSeen.containsKey(handle));
        previousTrackingPositions.keySet().removeIf(handle -> !trackingHandleLastSeen.containsKey(handle));
        previousTrackingSampleTicks.keySet().removeIf(handle -> !trackingHandleLastSeen.containsKey(handle));
        trackingSampleTicks.keySet().removeIf(handle -> !trackingHandleLastSeen.containsKey(handle));
    }

    private boolean hasHandleReference(int handle) {
        if (handle == 0) return false;
        for (BulletState state : states.values()) {
            if (state.trackingHandle() == handle) return true;
        }
        return false;
    }

    /** Blends sparse authoritative corrections to prevent visible positional snaps. */
    public boolean correct(int slot, int generation, Vec3 position, Vec3 velocity, double blend) {
        BulletState state = states.get(key(slot, generation));
        if (state == null) return false;
        state.applyCorrection(position, velocity, blend);
        return true;
    }

    public BulletState get(int slot, int generation) { return states.get(key(slot, generation)); }
    public int size() { return states.size(); }
    public long serverTick() { return serverTick; }
    public int epoch() { return epoch; }
    public List<BulletState> states() { return List.copyOf(states.values()); }
    /** Visits active states directly, avoiding a per-frame immutable snapshot allocation. */
    public void forEachState(Consumer<? super BulletState> visitor) {
        if (visitor == null) return;
        states.values().forEach(visitor);
    }

    /** Package-visible diagnostic seam used to verify lifecycle metadata does not leak. */
    int identityMetadataSize() {
        return velocitySampleTicks.size()
                + authoritativeVelocities.size() + authoritativeVelocityTicks.size()
                + appliedVelocityCorrectionTicks.size();
    }

    private void removeIdentityMetadata(long identity) {
        velocitySampleTicks.remove(identity);
        authoritativeVelocities.remove(identity);
        authoritativeVelocityTicks.remove(identity);
        appliedVelocityCorrectionTicks.remove(identity);
    }
    private static long key(int slot, int generation) { return ((long) generation << 32) ^ (slot & 0xFFFFFFFFL); }
}
