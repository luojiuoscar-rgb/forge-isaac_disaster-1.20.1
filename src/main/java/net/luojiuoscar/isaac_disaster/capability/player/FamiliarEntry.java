package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stores the requested count and accepted entity UUIDs for one familiar entity type.
 */
public class FamiliarEntry {
    private final ResourceLocation type;
    private int count;
    private final List<UUID> entityIds;

    public FamiliarEntry(ResourceLocation type) {
        this(type, 0, new ArrayList<>());
    }

    public FamiliarEntry(ResourceLocation type, int count, List<UUID> entityIds) {
        this.type = type;
        this.count = Math.max(0, count);
        this.entityIds = new ArrayList<>();
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

    public List<UUID> getEntityIds() {
        return entityIds;
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
    public static FamiliarEntry loadNBT(CompoundTag tag) {
        ResourceLocation type = ResourceLocation.parse(tag.getString("Type"));
        int count = tag.getInt("Count");
        return new FamiliarEntry(type, count, List.of());
    }
}
