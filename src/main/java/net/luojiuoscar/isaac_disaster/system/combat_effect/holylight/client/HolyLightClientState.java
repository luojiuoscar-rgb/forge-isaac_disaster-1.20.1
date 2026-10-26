package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightPhase;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightTimeline;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/** Pure client lifecycle and replay policy. Time advances only when the client simulation does. */
public final class HolyLightClientState {
    private final Map<Long, Entry> entries = new HashMap<>();
    private final Map<Long, Seen> seen = new HashMap<>();
    private final Collection<Entry> view = Collections.unmodifiableCollection(entries.values());
    private String dimension;
    private Integer epoch;
    private Integer epochFloor;
    private double now;
    private double renderNow;
    private long retiredThroughId;

    public void useDimension(String dimension) {
        if (!Objects.equals(this.dimension, dimension)) {
            clearForWorldUnload();
            this.dimension = dimension;
        }
    }

    /** Reset packets establish sessions. A repeated reset cannot reset cues or rewind the clock. */
    public boolean synchronize(int packetEpoch, boolean reset, long serverTick) {
        if (dimension == null) return false;
        if (epoch == null || packetEpoch != epoch) {
            if (!reset || epochFloor != null && packetEpoch <= epochFloor) return false;
            entries.clear();
            seen.clear();
            retiredThroughId = 0;
            epoch = packetEpoch;
            epochFloor = packetEpoch;
            now = serverTick;
            renderNow = now;
        } else {
            now = Math.max(now, serverTick);
        }
        refresh();
        return true;
    }

    public Entry add(long id, long startTick, int warningTicks, boolean grounded) {
        if (epoch == null || id <= retiredThroughId) return null;
        Entry existing = entries.get(id);
        if (existing != null) return existing;
        Seen history = seen.get(id);
        if (history == null) {
            if (now - startTick >= HolyLightTimeline.MAX_DURATION) {
                retiredThroughId = Math.max(retiredThroughId, id);
                return null;
            }
            history = new Seen(startTick, warningTicks, grounded);
            seen.put(id, history);
        }
        if (history.expired) return null;
        Entry entry = new Entry(id, history);
        entry.update(now);
        if (entry.phase == HolyLightPhase.EXPIRED) return null;
        entries.put(id, entry);
        return entry;
    }

    /** Visibility exits discard geometry, retaining identity and cue history for this epoch. */
    public void remove(long id) {
        entries.remove(id);
    }

    public Entry get(long id) { return entries.get(id); }
    public Collection<Entry> entries() { return view; }
    public double now() { return now; }

    /** Partial-tick sampling remains monotonic, including the frame which opens the pause menu. */
    public double renderTime(float partialTick, boolean paused) {
        double partial = paused || !Float.isFinite(partialTick) ? 0 : Math.max(0, Math.min(1, partialTick));
        renderNow = Math.max(renderNow, now + partial);
        return renderNow;
    }

    public void tick(boolean paused) {
        if (epoch == null || paused) return;
        now++;
        refresh();
    }

    public void clear() {
        clearForWorldUnload();
        epochFloor = null;
    }

    public void clearForWorldUnload() {
        entries.clear();
        seen.clear();
        epoch = null;
        dimension = null;
        now = 0;
        renderNow = 0;
        retiredThroughId = 0;
    }

    private void refresh() {
        Iterator<Entry> iterator = entries.values().iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            entry.update(now);
            if (entry.phase == HolyLightPhase.EXPIRED) iterator.remove();
        }
        Iterator<Map.Entry<Long, Seen>> history = seen.entrySet().iterator();
        while (history.hasNext()) {
            var entry = history.next();
            // IDs and birth ticks increase together on the server. Retire only after the longest
            // possible lifecycle, so a newer skipped strike cannot hide an older normal strike.
            if (now - entry.getValue().startTick >= HolyLightTimeline.MAX_DURATION) {
                retiredThroughId = Math.max(retiredThroughId, entry.getKey());
                history.remove();
            }
        }
    }

    private static final class Seen {
        private final long startTick;
        private final int warningTicks;
        private final boolean grounded;
        private boolean warned;
        private boolean impacted;
        private boolean expired;

        private Seen(long startTick, int warningTicks, boolean grounded) {
            this.startTick = startTick;
            this.warningTicks = warningTicks;
            this.grounded = grounded;
        }
    }

    public static final class Entry {
        private final long id;
        private final Seen seen;
        private HolyLightPhase phase;
        private HolyLightPhase previousPhase;
        private boolean warningCue;
        private boolean impactCue;
        private double lastAge;

        private Entry(long id, Seen seen) {
            this.id = id;
            this.seen = seen;
        }

        public long id() { return id; }
        public HolyLightPhase phase() { return phase; }
        public HolyLightPhase previousPhase() { return previousPhase; }
        public double age(double serverNow) { return Math.max(lastAge, Math.max(0, serverNow - seen.startTick)); }

        public boolean takeWarningCue() {
            boolean cue = warningCue;
            warningCue = false;
            return cue;
        }

        public boolean takeImpactCue() {
            boolean cue = impactCue;
            impactCue = false;
            return cue;
        }

        private void update(double serverNow) {
            lastAge = age(serverNow);
            HolyLightPhase current = HolyLightTimeline.phase(lastAge, seen.warningTicks, seen.grounded);
            boolean firingFromWarning = phase == HolyLightPhase.WARNING && current == HolyLightPhase.FIRING;
            if (current != phase) {
                previousPhase = phase == null ? current : phase;
                phase = current;
            }
            if (phase == HolyLightPhase.WARNING && !seen.warned) {
                seen.warned = true;
                warningCue = true;
            }
            if (phase != HolyLightPhase.WARNING) warningCue = false;
            if (phase != HolyLightPhase.WARNING && !seen.impacted) {
                seen.impacted = true;
                impactCue = phase == HolyLightPhase.FIRING
                        // Clock corrections may skip the opening tick of an already observed warning.
                        // Only first sightings need the freshness limit to avoid replaying old impacts.
                        && (firingFromWarning
                        || HolyLightTimeline.phaseAge(lastAge, seen.warningTicks, seen.grounded) <= 1.0D);
            }
            if (phase == HolyLightPhase.EXPIRED) seen.expired = true;
        }
    }
}
