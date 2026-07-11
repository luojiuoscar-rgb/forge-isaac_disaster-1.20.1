package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Player capability that records which Isaac familiars should exist for the player.
 */
@AutoRegisterCapability
public class PlayerFamiliarData {
    private final Map<ResourceLocation, FamiliarEntry> familiars = new LinkedHashMap<>();
    private int nextSpawnIndex;

    /**
     * Sets the required count for a familiar type.
     */
    public void setCount(ResourceLocation type, int count) {
        FamiliarEntry entry = getOrCreateEntry(type);
        entry.setCount(count);
        if (entry.getCount() == 0 && entry.hasNoEntities()) {
            familiars.remove(type);
            normalizeSpawnIndex();
        }
    }

    /**
     * Adds a delta to the required count for a familiar type.
     */
    public void addCount(ResourceLocation type, int delta) {
        long nextCount = (long) getCount(type) + delta;
        setCount(type, (int) Math.max(0L, Math.min(Integer.MAX_VALUE, nextCount)));
    }

    /**
     * Sets every familiar requirement to zero while keeping entity UUIDs for the next reconciliation pass.
     */
    public void clearRequirements() {
        for (FamiliarEntry entry : familiars.values()) {
            entry.setCount(0);
        }
    }

    /**
     * Removes entries that no longer request or reference any familiar entities.
     */
    public void pruneEmptyEntries() {
        familiars.entrySet().removeIf(entry ->
                entry.getValue().getCount() == 0 && entry.getValue().hasNoEntities());
        normalizeSpawnIndex();
    }

    public int getCount(ResourceLocation type) {
        return familiars.containsKey(type) ? familiars.get(type).getCount() : 0;
    }

    public Optional<FamiliarEntry> getEntry(ResourceLocation type) {
        return Optional.ofNullable(familiars.get(type));
    }

    public FamiliarEntry getOrCreateEntry(ResourceLocation type) {
        return familiars.computeIfAbsent(type, FamiliarEntry::new);
    }

    /**
     * Records a familiar entity as owned by this player.
     */
    public void registerEntity(ResourceLocation type, UUID entityId) {
        getOrCreateEntry(type).addEntity(entityId);
    }

    /**
     * Removes a familiar entity UUID from its entry.
     */
    public void removeEntity(ResourceLocation type, UUID entityId) {
        FamiliarEntry entry = familiars.get(type);
        if (entry == null) return;

        entry.removeEntity(entityId);
        if (entry.getCount() == 0 && entry.hasNoEntities()) {
            familiars.remove(type);
            normalizeSpawnIndex();
        }
    }

    public boolean containsEntity(ResourceLocation type, UUID entityId) {
        FamiliarEntry entry = familiars.get(type);
        return entry != null && entry.containsEntity(entityId);
    }

    public int getIndex(ResourceLocation type, UUID entityId) {
        FamiliarEntry entry = familiars.get(type);
        return entry == null ? -1 : entry.getIndex(entityId);
    }

    public Collection<FamiliarEntry> getEntries() {
        return Collections.unmodifiableCollection(familiars.values());
    }

    /**
     * Returns the next unmet familiar requirement in stable insertion-order round-robin order.
     */
    public Optional<FamiliarEntry> getNextIncompleteEntry() {
        int size = familiars.size();
        if (size == 0) {
            nextSpawnIndex = 0;
            return Optional.empty();
        }

        normalizeSpawnIndex();
        FamiliarEntry wrappedCandidate = null;
        int wrappedIndex = -1;
        int index = 0;
        for (FamiliarEntry entry : familiars.values()) {
            if (entry.getEntityCount() < entry.getCount()) {
                if (index >= nextSpawnIndex) {
                    nextSpawnIndex = (index + 1) % size;
                    return Optional.of(entry);
                }
                if (wrappedCandidate == null) {
                    wrappedCandidate = entry;
                    wrappedIndex = index;
                }
            }
            index++;
        }

        if (wrappedCandidate != null) {
            nextSpawnIndex = (wrappedIndex + 1) % size;
            return Optional.of(wrappedCandidate);
        }
        return Optional.empty();
    }

    /**
     * Copies persistent familiar requirements from another player capability.
     */
    public void copyFrom(PlayerFamiliarData source) {
        familiars.clear();
        nextSpawnIndex = 0;
        for (FamiliarEntry entry : source.familiars.values()) {
            FamiliarEntry copied = new FamiliarEntry(entry.getType(), entry.getCount(), java.util.List.of());
            familiars.put(copied.getType(), copied);
        }
    }

    /**
     * Saves familiar requirements. Runtime entity UUIDs are intentionally not persistent.
     */
    public void saveNBTData(CompoundTag nbt) {
        ListTag entries = new ListTag();
        for (FamiliarEntry entry : familiars.values()) {
            entries.add(entry.saveNBT());
        }
        nbt.put("Familiars", entries);
    }

    /**
     * Loads familiar requirements. Real entities are recreated by the familiar reconciler.
     */
    public void loadNBTData(CompoundTag nbt) {
        familiars.clear();
        nextSpawnIndex = 0;
        if (!nbt.contains("Familiars", Tag.TAG_LIST)) return;

        ListTag entries = nbt.getList("Familiars", Tag.TAG_COMPOUND);
        for (Tag entryTag : entries) {
            if (!(entryTag instanceof CompoundTag compoundTag)) continue;
            FamiliarEntry.loadNBT(compoundTag).ifPresent(entry -> familiars.put(entry.getType(), entry));
        }
    }

    private void normalizeSpawnIndex() {
        if (familiars.isEmpty()) {
            nextSpawnIndex = 0;
        } else {
            nextSpawnIndex = Math.floorMod(nextSpawnIndex, familiars.size());
        }
    }
}
