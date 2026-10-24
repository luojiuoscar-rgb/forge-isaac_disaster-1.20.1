package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server;

import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-player synchronization visits; dimension round trips never reuse an older epoch. */
public final class GroundEffectSessions {
    private final Map<UUID, Session> sessions = new HashMap<>();
    private int nextEpoch = 1;

    public Session get(UUID player, ResourceLocation dimension) {
        Session current = sessions.get(player);
        if (current == null || !current.dimension.equals(dimension)) {
            current = new Session(nextEpoch++, dimension);
            sessions.put(player, current);
        }
        return current;
    }

    public void remove(UUID player) { sessions.remove(player); }
    public void removeDimension(ResourceLocation dimension) {
        sessions.values().removeIf(session -> session.dimension.equals(dimension));
    }
    public void clear() { sessions.clear(); }

    public static final class Session {
        private final int epoch;
        private final ResourceLocation dimension;
        private final Map<Long, Integer> visible = new HashMap<>();
        boolean initialized;
        int lastClockTick;

        private Session(int epoch, ResourceLocation dimension) {
            this.epoch = epoch;
            this.dimension = dimension;
        }
        public int epoch() { return epoch; }
        public Map<Long, Integer> visible() { return visible; }
    }
}
