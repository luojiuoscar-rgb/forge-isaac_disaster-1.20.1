package net.luojiuoscar.isaac_disaster.groundeffect.client;

import net.luojiuoscar.isaac_disaster.networking.packet.groundeffect.GroundEffectSnapshotBatchS2CPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.Comparator;

/** Client stream with epoch and slot-generation protection for transient ground effects. */
public final class GroundEffectStream {
    private final Map<Long, ClientGroundEffectState> states = new HashMap<>();
    private final Map<Integer, Integer> latestGenerations = new HashMap<>();
    private final Map<Integer, Integer> visibilityPermits = new HashMap<>();
    private final GroundEffectClock clock;
    private List<ClientGroundEffectState> renderStates = List.of();
    private boolean renderOrderDirty = true;
    private boolean awaitingSession = true;
    private int epoch = Integer.MIN_VALUE;

    public GroundEffectStream() { this(System::nanoTime); }
    public GroundEffectStream(LongSupplier nanos) { clock = new GroundEffectClock(nanos); }

    public boolean beginSession(int value) {
        if (value <= 0 || value < epoch || awaitingSession && value == epoch) return false;
        if (!awaitingSession && value == epoch) return true;
        clearState();
        epoch = value;
        awaitingSession = false;
        return true;
    }

    public boolean acceptEpoch(int value) {
        return !awaitingSession && value == epoch;
    }

    public void synchronize(int serverTick) { clock.observe(serverTick); }

    public ClientGroundEffectState spawn(GroundEffectSnapshotBatchS2CPacket.Entry entry) {
        return spawn(entry, (int) clock.now());
    }

    public ClientGroundEffectState spawn(GroundEffectSnapshotBatchS2CPacket.Entry entry, int serverTick) {
        Integer latest = latestGenerations.get(entry.slot());
        if (latest != null && entry.generation() < latest) return null;
        if (latest != null && entry.generation() == latest
                && !Integer.valueOf(entry.generation()).equals(visibilityPermits.get(entry.slot()))) return null;
        if (latest != null) {
            long previous = ((long) latest << 32) | (entry.slot() & 0xFFFFFFFFL);
            states.remove(previous);
        }
        visibilityPermits.remove(entry.slot());
        latestGenerations.put(entry.slot(), entry.generation());
        long identity = ((long) entry.generation() << 32) | (entry.slot() & 0xFFFFFFFFL);
        states.remove(identity);
        ClientGroundEffectState state = state(entry);
        clock.observe(serverTick);
        state.alignTime(clock.unwrap(serverTick), clock.now());
        states.put(identity, state);
        renderOrderDirty = true;
        return state;
    }

    public boolean applyUpdate(GroundEffectSnapshotBatchS2CPacket.Entry snapshot) {
        return applyUpdate(snapshot, (int) clock.now());
    }

    public boolean applyUpdate(GroundEffectSnapshotBatchS2CPacket.Entry snapshot, int serverTick) {
        long identity = ((long) snapshot.generation() << 32) | (snapshot.slot() & 0xFFFFFFFFL);
        ClientGroundEffectState current = states.get(identity);
        if (current == null || snapshot.revision() <= current.revision()) return false;
        clock.observe(serverTick);
        ClientGroundEffectState changed = state(snapshot);
        changed.alignTime(clock.unwrap(serverTick), clock.now());
        states.put(identity, changed);
        renderOrderDirty = true;
        return true;
    }

    private static ClientGroundEffectState state(GroundEffectSnapshotBatchS2CPacket.Entry entry) {
        return new ClientGroundEffectState(entry.slot(), entry.generation(), entry.position(), entry.radius(),
                entry.age(), entry.lifetime(), entry.creationOrder(), entry.ownerUuid(), entry.typeId(),
                entry.color(), entry.shapeSeed(), entry.revision(), entry.coverage());
    }

    public boolean despawn(long identity) {
        boolean removed = states.remove(identity) != null;
        renderOrderDirty |= removed;
        return removed;
    }

    public boolean visibilityExit(int slot, int generation) {
        long identity = ((long) generation << 32) | (slot & 0xFFFFFFFFL);
        boolean removed = states.remove(identity) != null;
        renderOrderDirty |= removed;
        if (removed || Integer.valueOf(generation).equals(latestGenerations.get(slot))) {
            visibilityPermits.put(slot, generation);
            return removed;
        }
        return false;
    }

    public void tick() {
        double tick = clock.now();
        states.values().forEach(state -> state.updateAge(tick));
    }

    public void clear() {
        clearState();
        epoch = Integer.MIN_VALUE;
        awaitingSession = true;
    }

    public void clearForWorldUnload() {
        clearState();
        awaitingSession = true;
    }

    public int size() { return states.size(); }
    public int epoch() { return epoch; }
    public ClientGroundEffectState get(int slot, int generation) {
        return states.get(((long) generation << 32) | (slot & 0xFFFFFFFFL));
    }
    public List<ClientGroundEffectState> states() {
        if (renderOrderDirty) {
            renderStates = states.values().stream()
                    .sorted(Comparator.comparingLong(ClientGroundEffectState::creationOrder)).toList();
            renderOrderDirty = false;
        }
        return renderStates;
    }
    public void forEachState(Consumer<ClientGroundEffectState> visitor) { states.values().forEach(visitor); }

    private void clearState() {
        states.clear();
        latestGenerations.clear();
        visibilityPermits.clear();
        clock.clear();
        renderStates = List.of();
        renderOrderDirty = true;
    }
}
