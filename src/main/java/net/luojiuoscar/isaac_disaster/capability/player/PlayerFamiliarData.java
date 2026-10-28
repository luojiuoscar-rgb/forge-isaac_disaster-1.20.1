package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarLimitGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/** Persistent ownership counts, with per-player group allocations and runtime entity registrations. */
@AutoRegisterCapability
public class PlayerFamiliarData {
    private final Map<ResourceLocation, FamiliarEntry> familiars = new LinkedHashMap<>();
    private final Map<FamiliarLimitGroup, FamiliarLimitGroupInstance> groups = new IdentityHashMap<>();
    private final Function<ResourceLocation, FamiliarEntityType> typeResolver;
    private final LinkedHashSet<UUID> pendingRecalls = new LinkedHashSet<>();
    private final Set<UUID> registeredEntityIds = new LinkedHashSet<>();
    private int nextSpawnIndex;
    private long lastAdmissionTick = Long.MIN_VALUE;
    @Nullable
    private SpawnReservation pendingSpawn;

    public PlayerFamiliarData() {
        this(FamiliarHelper::getFamiliarType, FamiliarHelper.getRegisteredFamiliarLimitGroups());
    }

    PlayerFamiliarData(Function<ResourceLocation, FamiliarEntityType> typeResolver, Collection<FamiliarLimitGroup> definitions) {
        this.typeResolver = Objects.requireNonNull(typeResolver);
        for (FamiliarLimitGroup definition : definitions) groups.put(definition, new FamiliarLimitGroupInstance(definition));
    }

    public void setCount(ResourceLocation type, int count) {
        if (pendingSpawn != null && pendingSpawn.type.equals(type)) pendingSpawn = null;
        FamiliarEntry entry = getOrCreateEntry(type);
        entry.setCount(count);
        FamiliarEntityType descriptor = typeResolver.apply(type);
        if (descriptor != null) {
            applyOwnLimit(entry, descriptor);
            FamiliarLimitGroupInstance group = groups.get(descriptor.getGroup());
            if (group != null) group.reconcile(familiars.values(), typeResolver);
        }
        pruneEmptyEntries();
    }

    public void addCount(ResourceLocation type, int delta) {
        long nextCount = (long) getCount(type) + delta;
        setCount(type, (int) Math.max(0L, Math.min(Integer.MAX_VALUE, nextCount)));
    }

    /** Keeps UUIDs until reconciliation can discard the corresponding world entities. */
    public void clearRequirements() {
        pendingSpawn = null;
        for (FamiliarEntry entry : familiars.values()) entry.setCount(0);
        for (FamiliarLimitGroupInstance group : groups.values()) group.clear();
    }

    public void pruneEmptyEntries() {
        familiars.entrySet().removeIf(item -> item.getValue().getCount() == 0 && item.getValue().hasNoEntities());
    }

    public int getCount(ResourceLocation type) {
        FamiliarEntry entry = familiars.get(type);
        return entry == null ? 0 : entry.getCount();
    }

    public int getEffectiveCount(ResourceLocation type) {
        FamiliarEntry entry = familiars.get(type);
        FamiliarEntityType descriptor = typeResolver.apply(type);
        if (entry == null || descriptor == null) return 0;
        if (descriptor.getGroup() == null) return Math.min(entry.getCount(), descriptor.getLimit());
        FamiliarLimitGroupInstance group = groups.get(descriptor.getGroup());
        return group == null ? 0 : group.getQuota(type);
    }

    public Optional<FamiliarEntry> getEntry(ResourceLocation type) { return Optional.ofNullable(familiars.get(type)); }

    public FamiliarEntry getOrCreateEntry(ResourceLocation type) {
        return familiars.computeIfAbsent(type, FamiliarEntry::new);
    }

    /** Links an entity to an already granted slot; unlinking never releases that allocation. */
    public void registerEntity(ResourceLocation type, UUID entityId) {
        FamiliarEntry entry = getOrCreateEntry(type);
        if (entityId == null || entry.containsEntity(entityId)) return;
        if (entry.getEntityCount() >= getEffectiveCount(type))
            throw new IllegalStateException("No allocated familiar slot for " + type);
        entry.addEntity(entityId);
        registeredEntityIds.add(entityId);
    }

    public void removeEntity(ResourceLocation type, UUID entityId) {
        if (pendingSpawn != null && pendingSpawn.type.equals(type) && pendingSpawn.entityId.equals(entityId)) pendingSpawn = null;
        pendingRecalls.remove(entityId);
        FamiliarEntry entry = familiars.get(type);
        if (entry == null) return;
        if (entry.removeEntity(entityId)) registeredEntityIds.remove(entityId);
        if (entry.getCount() == 0 && entry.hasNoEntities()) familiars.remove(type);
    }

    public boolean containsEntity(ResourceLocation type, UUID entityId) {
        if (pendingSpawn != null && pendingSpawn.type.equals(type) && pendingSpawn.entityId.equals(entityId)) return true;
        FamiliarEntry entry = familiars.get(type);
        return entry != null && entry.containsEntity(entityId);
    }

    public Collection<FamiliarEntry> getEntries() { return Collections.unmodifiableCollection(familiars.values()); }

    /** Unmodifiable runtime roster in registration order across all familiar types. */
    public Collection<UUID> getEntityIdsInAdmissionOrder() {
        return Collections.unmodifiableSet(registeredEntityIds);
    }

    /** Unlinks all runtime entities after disposal, without changing granted counts. */
    public void clearRuntimeEntities() {
        pendingSpawn = null;
        for (FamiliarEntry entry : familiars.values()) entry.clearEntityIds();
        registeredEntityIds.clear();
        pendingRecalls.clear();
    }

    /** Round-robin entity restoration; quota was already assigned when requirements changed. */
    public Optional<FamiliarEntry> getNextIncompleteEntry() {
        int size = familiars.size();
        if (size == 0) return Optional.empty();
        nextSpawnIndex = Math.floorMod(nextSpawnIndex, size);
        FamiliarEntry wrapped = null;
        int wrappedIndex = -1;
        int index = 0;
        for (FamiliarEntry entry : familiars.values()) {
            if (entry.getEntityCount() < getEffectiveCount(entry.getType())) {
                if (index >= nextSpawnIndex) {
                    nextSpawnIndex = (index + 1) % size;
                    return Optional.of(entry);
                }
                if (wrapped == null) {
                    wrapped = entry;
                    wrappedIndex = index;
                }
            }
            index++;
        }
        if (wrapped != null) nextSpawnIndex = (wrappedIndex + 1) % size;
        return Optional.ofNullable(wrapped);
    }

    /** Runtime-only FIFO; repeated distance checks cannot enqueue an entity twice. */
    public void queueRecall(UUID entityId) { pendingRecalls.add(entityId); }

    public UUID pollRecall() {
        var iterator = pendingRecalls.iterator();
        if (!iterator.hasNext()) return null;
        UUID entityId = iterator.next();
        iterator.remove();
        return entityId;
    }

    /** Recalls and new spawns share one admission per server tick. */
    public boolean beginAdmission(long tick) {
        if (lastAdmissionTick == tick) return false;
        lastAdmissionTick = tick;
        return true;
    }

    public void clearPendingRecalls() { pendingRecalls.clear(); }

    /** Copies persistent counts and insertion order, never runtime allocations or UUIDs. */
    public void copyFrom(PlayerFamiliarData source) {
        List<FamiliarEntry> copied = source.familiars.values().stream()
                .map(entry -> new FamiliarEntry(entry.getType(), entry.getCount(), List.of())).toList();
        familiars.clear();
        for (FamiliarEntry entry : copied) familiars.put(entry.getType(), entry);
        rebuildRuntimeState();
    }

    public void saveNBTData(CompoundTag nbt) {
        ListTag entries = new ListTag();
        for (FamiliarEntry entry : familiars.values()) entries.add(entry.saveNBT());
        nbt.put("Familiars", entries);
    }

    public void loadNBTData(CompoundTag nbt) {
        familiars.clear();
        if (nbt.contains("Familiars", Tag.TAG_LIST)) {
            for (Tag entryTag : nbt.getList("Familiars", Tag.TAG_COMPOUND)) {
                if (entryTag instanceof CompoundTag tag)
                    FamiliarEntry.loadNBT(tag).ifPresent(entry -> familiars.put(entry.getType(), entry));
            }
        }
        rebuildRuntimeState();
    }

    private void rebuildRuntimeState() {
        pendingSpawn = null;
        pendingRecalls.clear();
        registeredEntityIds.clear();
        lastAdmissionTick = Long.MIN_VALUE;
        nextSpawnIndex = 0;
        for (FamiliarLimitGroupInstance group : groups.values()) group.clear();
        for (FamiliarEntry entry : familiars.values()) {
            FamiliarEntityType descriptor = typeResolver.apply(entry.getType());
            if (descriptor != null) applyOwnLimit(entry, descriptor);
        }
        for (FamiliarLimitGroupInstance group : groups.values()) group.reconcile(familiars.values(), typeResolver);
        pruneEmptyEntries();
    }

    /**
     * Reserves membership for an immediate world join without consuming persistent counts or replacing old entities.
     * Only one immediate join per owner may be in progress; callers must close the reservation.
     */
    @Nullable
    public SpawnReservation reserveSpawn(ResourceLocation type, UUID entityId) {
        if (pendingSpawn != null || entityId == null || registeredEntityIds.contains(entityId) || !canAddImmediately(type)) return null;
        pendingSpawn = new SpawnReservation(type, entityId);
        return pendingSpawn;
    }

    private boolean canAddImmediately(ResourceLocation type) {
        if (typeResolver.apply(type) == null || getCount(type) == Integer.MAX_VALUE) return false;
        FamiliarEntry current = familiars.get(type);
        if (current != null && current.getEntityCount() > getEffectiveCount(type)) return false;
        PlayerFamiliarData preview = new PlayerFamiliarData(typeResolver, List.of());
        for (FamiliarEntry entry : familiars.values()) {
            preview.familiars.put(entry.getType(), new FamiliarEntry(entry.getType(), entry.getCount(), entry.getEntityIds()));
        }
        groups.forEach((definition, allocation) -> preview.groups.put(definition, allocation.copy()));
        preview.addCount(type, 1);
        return preview.getCount(type) == getCount(type) + 1
                && preview.getEffectiveCount(type) == getEffectiveCount(type) + 1;
    }

    /** A tentative join becomes persistent only when the world accepts the entity and commit succeeds. */
    public final class SpawnReservation implements AutoCloseable {
        private final ResourceLocation type;
        private final UUID entityId;

        private SpawnReservation(ResourceLocation type, UUID entityId) {
            this.type = type;
            this.entityId = entityId;
        }

        public boolean commit() {
            if (pendingSpawn != this || !canAddImmediately(type)) return false;
            pendingSpawn = null;
            addCount(type, 1);
            registerEntity(type, entityId);
            return true;
        }

        @Override
        public void close() {
            if (pendingSpawn == this) pendingSpawn = null;
        }
    }

    private static void applyOwnLimit(FamiliarEntry entry, FamiliarEntityType type) {
        if (!type.retainOverflow()) entry.setCount(Math.min(entry.getCount(), type.getLimit()));
    }
}
