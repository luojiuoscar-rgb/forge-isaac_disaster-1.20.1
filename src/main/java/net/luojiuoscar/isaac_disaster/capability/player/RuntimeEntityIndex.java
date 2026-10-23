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
    private final Map<ResourceLocation, UUID> entityIds = new HashMap<>();

    @Nullable
    public UUID getId(ResourceLocation key) {
        return entityIds.get(key);
    }

    public void register(ResourceLocation key, UUID entityId) {
        entityIds.put(key, entityId);
    }

    /** Resolves a slot in the current world, pruning missing, removed or mismatched entities. */
    @Nullable
    public <T extends Entity> T getEntity(ResourceLocation key, ServerLevel level, Class<T> type) {
        UUID entityId = entityIds.get(key);
        if (entityId == null) return null;
        Entity entity = level.getEntity(entityId);
        if (entity == null || entity.isRemoved() || !type.isInstance(entity)) {
            entityIds.remove(key, entityId);
            return null;
        }
        return type.cast(entity);
    }

    /** An old entity can release its slot without unregistering a newer replacement. */
    public boolean unregister(ResourceLocation key, UUID expectedId) {
        return entityIds.remove(key, expectedId);
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
