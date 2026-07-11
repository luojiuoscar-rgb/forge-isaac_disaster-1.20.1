package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores the requested count and accepted entity UUIDs for one familiar entity type.
 */
public class FamiliarEntry {
    private final ResourceLocation type;
    private int count;
    private final List<UUID> entityIds;
    private final List<UUID> entityIdsView;

    public FamiliarEntry(ResourceLocation type) {
        this(type, 0, new ArrayList<>());
    }

    public FamiliarEntry(ResourceLocation type, int count, List<UUID> entityIds) {
        this.type = type;
        this.count = Math.max(0, count);
        this.entityIds = new ArrayList<>();
        this.entityIdsView = Collections.unmodifiableList(this.entityIds);
        for (UUID entityId : entityIds) {
            addEntity(entityId);
        }
    }

    public ResourceLocation getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = Math.max(0, count);
    }

    /**
     * Returns an unmodifiable live view of runtime familiar entity UUIDs in formation order.
     */
    public List<UUID> getEntityIds() {
        return entityIdsView;
    }

    public int getEntityCount() {
        return entityIds.size();
    }

    public UUID getEntityId(int index) {
        return entityIds.get(index);
    }

    public boolean hasNoEntities() {
        return entityIds.isEmpty();
    }

    public boolean containsEntity(UUID entityId) {
        return entityIds.contains(entityId);
    }

    /**
     * Adds an entity UUID if it is not already recorded.
     */
    public void addEntity(UUID entityId) {
        if (entityId == null || entityIds.contains(entityId)) return;
        entityIds.add(entityId);
    }

    /**
     * Removes an entity UUID from this entry.
     */
    public boolean removeEntity(UUID entityId) {
        return entityIds.remove(entityId);
    }

    /**
     * Removes and returns the runtime UUID at the requested formation index.
     */
    public UUID removeEntityAt(int index) {
        return entityIds.remove(index);
    }

    /**
     * Clears all runtime UUIDs while preserving the persistent requested count.
     */
    public void clearEntityIds() {
        entityIds.clear();
    }

    public int getIndex(UUID entityId) {
        return entityIds.indexOf(entityId);
    }

    /**
     * Serializes only persistent familiar requirements into player capability NBT.
     */
    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type.toString());
        tag.putInt("Count", count);
        return tag;
    }

    /**
     * Loads persistent familiar requirements from player capability NBT.
     */
    public static Optional<FamiliarEntry> loadNBT(CompoundTag tag) {
        ResourceLocation type = ResourceLocation.tryParse(tag.getString("Type"));
        if (type == null) return Optional.empty();

        int count = tag.getInt("Count");
        return Optional.of(new FamiliarEntry(type, count, List.of()));
    }
}
