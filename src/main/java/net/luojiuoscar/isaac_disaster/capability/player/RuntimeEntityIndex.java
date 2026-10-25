package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Transient entities owned by player abilities; deliberately excluded from saves and respawn copies. */
public final class RuntimeEntityIndex {
    private record Slot(ResourceLocation key, int index) {}
    private final Map<Slot, UUID> entityIds = new HashMap<>();

    @Nullable
    public UUID getId(ResourceLocation key) {
        return getId(key, 0);
    }

    @Nullable
    public UUID getId(ResourceLocation key, int slot) {
        return entityIds.get(new Slot(key, slot));
    }

    public void register(ResourceLocation key, UUID entityId) {
        register(key, 0, entityId);
    }

    public void register(ResourceLocation key, int slot, UUID entityId) {
        entityIds.put(new Slot(key, slot), entityId);
    }

    /** Resolves a slot in the current world, pruning missing, removed or mismatched entities. */
    @Nullable
    public <T extends Entity> T getEntity(ResourceLocation key, ServerLevel level, Class<T> type) {
        return getEntity(key, 0, level, type);
    }

    @Nullable
    public <T extends Entity> T getEntity(ResourceLocation key, int slot, ServerLevel level, Class<T> type) {
        Slot identity = new Slot(key, slot);
        UUID entityId = entityIds.get(identity);
        if (entityId == null) return null;
        Entity entity = level.getEntity(entityId);
        if (entity == null || entity.isRemoved() || !type.isInstance(entity)) {
            entityIds.remove(identity, entityId);
            return null;
        }
        return type.cast(entity);
    }

    /** An old entity can release its slot without unregistering a newer replacement. */
    public boolean unregister(ResourceLocation key, UUID expectedId) {
        return unregister(key, 0, expectedId);
    }

    public boolean unregister(ResourceLocation key, int slot, UUID expectedId) {
        return entityIds.remove(new Slot(key, slot), expectedId);
    }

    public void clear() {
        entityIds.clear();
    }

    /** Clears references before removing entities so removal callbacks cannot mutate the iteration. */
    public void discardAll(@Nullable ServerLevel level) {
        Set<UUID> ids = Set.copyOf(entityIds.values());
        clear();
        if (level == null) return;
        for (UUID entityId : ids) {
            Entity entity = level.getEntity(entityId);
            if (entity != null && !entity.isRemoved()) entity.discard();
        }
    }
}
