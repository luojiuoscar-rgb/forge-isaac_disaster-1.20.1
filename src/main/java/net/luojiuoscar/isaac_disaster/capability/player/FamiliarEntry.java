package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

/**
 * Stores the requested count and accepted entity UUIDs for one familiar entity type.
 */
public class FamiliarEntry {
    private final ResourceLocation type;
    private final FamiliarRequirements requirements = new FamiliarRequirements();
    private final List<UUID> entityIds;
    private final List<UUID> entityIdsView;
    private final Map<UUID, ResourceLocation> entitySources = new LinkedHashMap<>();

    public FamiliarEntry(ResourceLocation type) {
        this.type = type;
        this.entityIds = new ArrayList<>();
        this.entityIdsView = Collections.unmodifiableList(this.entityIds);
    }

    public ResourceLocation getType() {
        return type;
    }

    public int getCount() {
        return requirements.getTotal();
    }

    public int getCount(ResourceLocation source) { return requirements.getCount(source); }

    FamiliarRequirements requirements() { return requirements; }

    public ResourceLocation getEntitySource(UUID entityId) { return entitySources.get(entityId); }

    public int getEntityCount(ResourceLocation source) {
        return (int) entitySources.values().stream().filter(source::equals).count();
    }

    public Optional<ResourceLocation> getIncompleteSource() {
        return requirements.getSources().keySet().stream()
                .filter(source -> getEntityCount(source) < getCount(source)).findFirst();
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
        return entitySources.containsKey(entityId);
    }

    /**
     * Adds an entity UUID if it is not already recorded.
     */
    public void addEntity(UUID entityId, ResourceLocation source) {
        if (entityId == null || entitySources.putIfAbsent(entityId, Objects.requireNonNull(source)) != null) return;
        entityIds.add(entityId);
    }

    /**
     * Removes an entity UUID from this entry.
     */
    public boolean removeEntity(UUID entityId) {
        if (entitySources.remove(entityId) == null) return false;
        return entityIds.remove(entityId);
    }

    /**
     * Clears all runtime UUIDs while preserving the persistent requested count.
     */
    public void clearEntityIds() {
        entityIds.clear();
        entitySources.clear();
    }

    /**
     * Serializes only persistent familiar requirements into player capability NBT.
     */
    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type.toString());
        tag.put("Requirements", requirements.saveNBT());
        return tag;
    }

    /**
     * Loads persistent familiar requirements from player capability NBT.
     */
    public static Optional<FamiliarEntry> loadNBT(CompoundTag tag) {
        ResourceLocation type = ResourceLocation.tryParse(tag.getString("Type"));
        if (type == null) return Optional.empty();

        FamiliarEntry entry = new FamiliarEntry(type);
        entry.requirements.loadNBT(tag.getList("Requirements", Tag.TAG_COMPOUND));
        return Optional.of(entry);
    }
}
