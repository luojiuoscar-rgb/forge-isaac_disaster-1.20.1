package net.luojiuoscar.isaac_disaster.manager.data;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.block_entity.misc.ItemDisplayContainerBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockData extends SavedData {
    // --------------------------
    // 各类方块数据
    // --------------------------
    private final Set<BlockPos> pedestals = new HashSet<>();
    private final Set<BlockPos> pendingPedestalClears = new HashSet<>();
    private final Map<BlockPos, PendingPedestalUpdate> pendingPedestalUpdates = new HashMap<>();
    private final Map<ResourceLocation, Set<BlockPos>> identifiers = new HashMap<>();
    private final Set<BlockPos> itemBlocks = new HashSet<>(); // block with item display

    // --------------------------
    // Pedestal 操作
    // --------------------------
    public void addPedestal(BlockPos pos) {
        if (pedestals.add(pos.immutable())) setDirty();
    }

    public void removePedestal(BlockPos pos) {
        if (pedestals.remove(pos)) {
            setDirty();
        }
    }

    public Set<BlockPos> getAllPedestals() {
        return new HashSet<>(pedestals);
    }

    /** An unloaded linked choice is consumed as soon as its block entity loads. */
    public void queuePedestalClear(BlockPos pos) {
        if (pendingPedestalClears.add(pos.immutable())) setDirty();
    }

    public boolean takePendingPedestalClear(BlockPos pos) {
        boolean removed = pendingPedestalClears.remove(pos);
        if (removed) setDirty();
        return removed;
    }

    public record PendingPedestalUpdate(Boolean locked, Set<BlockPos> linkedPositions, Set<BlockPos> removedLinks) {
        public PendingPedestalUpdate {
            linkedPositions = linkedPositions == null ? null : sanitizePositions(linkedPositions, "linkedPositions");
            if (removedLinks == null) {
                IsaacDisaster.LOGGER.warn("Null removedLinks in pending pedestal update; using an empty set");
                removedLinks = Set.of();
            } else {
                removedLinks = sanitizePositions(removedLinks, "removedLinks");
            }
        }
    }

    private static Set<BlockPos> sanitizePositions(Set<BlockPos> positions, String field) {
        Set<BlockPos> sanitized = new HashSet<>();
        for (BlockPos pos : positions) {
            if (pos == null) {
                IsaacDisaster.LOGGER.warn("Null position in pending pedestal update {}; skipping entry", field);
            } else {
                sanitized.add(pos.immutable());
            }
        }
        return Set.copyOf(sanitized);
    }

    private PendingPedestalUpdate pendingUpdate(BlockPos pos) {
        return pendingPedestalUpdates.getOrDefault(pos, new PendingPedestalUpdate(null, null, Set.of()));
    }

    public void queuePedestalLock(BlockPos pos, boolean locked) {
        PendingPedestalUpdate old = pendingUpdate(pos);
        pendingPedestalUpdates.put(pos.immutable(), new PendingPedestalUpdate(locked, old.linkedPositions(), old.removedLinks()));
        setDirty();
    }

    public void queuePedestalLinks(BlockPos pos, Set<BlockPos> positions) {
        PendingPedestalUpdate old = pendingUpdate(pos);
        pendingPedestalUpdates.put(pos.immutable(), new PendingPedestalUpdate(old.locked(), positions, Set.of()));
        setDirty();
    }

    public void queuePedestalUnlink(BlockPos pos, BlockPos removed) {
        PendingPedestalUpdate old = pendingUpdate(pos);
        Set<BlockPos> removals = new HashSet<>(old.removedLinks());
        removals.add(removed.immutable());
        pendingPedestalUpdates.put(pos.immutable(), new PendingPedestalUpdate(old.locked(), old.linkedPositions(), removals));
        setDirty();
    }

    public PendingPedestalUpdate takePendingPedestalUpdate(BlockPos pos) {
        PendingPedestalUpdate update = pendingPedestalUpdates.remove(pos);
        if (update != null) setDirty();
        return update;
    }

    // --------------------------
    // ItemBlock 操作
    // --------------------------
    public void addItemBlock(BlockPos pos) {
        if (itemBlocks.add(pos.immutable())) setDirty();
    }

    public void removeItemBlock(BlockPos pos) {
        if (itemBlocks.remove(pos)) {
            setDirty();
        }
    }

    public Set<BlockPos> getAllItemBlocks() {
        return new HashSet<>(itemBlocks);
    }

    /** Removes stale entries only when their chunks are currently loaded. */
    public void reconcileLoaded(ServerLevel level) {
        boolean changed = false;
        for (BlockPos pos : new HashSet<>(pedestals)) {
            if (level.hasChunkAt(pos)
                    && !(level.getBlockEntity(pos) instanceof PedestalBlockEntity)) {
                changed |= pedestals.remove(pos);
                changed |= pendingPedestalClears.remove(pos);
                changed |= pendingPedestalUpdates.remove(pos) != null;
            }
        }
        for (BlockPos pos : new HashSet<>(itemBlocks)) {
            if (!level.hasChunkAt(pos)) continue;
            var blockEntity = level.getBlockEntity(pos);
            boolean hasDisplay = blockEntity instanceof PedestalBlockEntity pedestal
                    ? !pedestal.getItem().isEmpty()
                    : blockEntity instanceof ItemDisplayContainerBlockEntity display
                    && !display.getItemDisplayList().isEmpty();
            if (!hasDisplay) changed |= itemBlocks.remove(pos);
        }
        if (changed) setDirty();
    }

    // --------------------------
    // Identifier 操作
    // --------------------------
    public void addIdentifier(ResourceLocation type, BlockPos pos) {
        identifiers.computeIfAbsent(type, k -> new HashSet<>()).add(pos.immutable());
        setDirty();
    }

    public void removeIdentifier(ResourceLocation type, BlockPos pos) {
        Set<BlockPos> set = identifiers.get(type);
        if (set != null && set.remove(pos)) {
            if (set.isEmpty()) identifiers.remove(type);
            setDirty();
        }
    }

    public Set<BlockPos> getIdentifiers(ResourceLocation type) {
        return identifiers.containsKey(type)
                ? new HashSet<>(identifiers.get(type))
                : Set.of();
    }

    public Map<ResourceLocation, Set<BlockPos>> getAllIdentifiers() {
        Map<ResourceLocation, Set<BlockPos>> copy = new HashMap<>();
        identifiers.forEach((k, v) -> copy.put(k, new HashSet<>(v)));
        return copy;
    }



    // --------------------------
    // NBT 加载 & 保存
    // --------------------------
    public static BlockData load(CompoundTag tag) {
        BlockData manager = new BlockData();

        // Pedestals
        ListTag pedestalList = tag.getList("Pedestals", Tag.TAG_COMPOUND);
        for (Tag t : pedestalList) {
            manager.pedestals.add(NbtUtils.readBlockPos((CompoundTag) t));
        }

        for (Tag t : tag.getList("PendingPedestalClears", Tag.TAG_COMPOUND)) {
            manager.pendingPedestalClears.add(NbtUtils.readBlockPos((CompoundTag) t));
        }
        for (Tag t : tag.getList("PendingPedestalUpdates", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            manager.pendingPedestalUpdates.put(NbtUtils.readBlockPos(entry.getCompound("Position")),
                    new PendingPedestalUpdate(entry.contains("Locked") ? entry.getBoolean("Locked") : null,
                            entry.contains("Links") ? readPositions(entry.getList("Links", Tag.TAG_COMPOUND)) : null,
                            readPositions(entry.getList("RemovedLinks", Tag.TAG_COMPOUND))));
        }
        // Identifiers
        ListTag identifierList = tag.getList("Identifiers", Tag.TAG_COMPOUND);
        for (Tag t : identifierList) {
            CompoundTag entry = (CompoundTag) t;
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("Type"));
            if (id == null) {
                IsaacDisaster.LOGGER.warn("Invalid block identifier '{}' in saved BlockData; skipping entry", entry.getString("Type"));
                continue;
            }

            ListTag posList = entry.getList("Positions", Tag.TAG_COMPOUND);
            Set<BlockPos> positions = new HashSet<>();
            for (Tag posTag : posList) {
                positions.add(NbtUtils.readBlockPos((CompoundTag) posTag));
            }
            manager.identifiers.put(id, positions);
        }

        // ItemBlocks
        ListTag itemList = tag.getList("ItemBlocks", Tag.TAG_COMPOUND);
        for (Tag t : itemList) {
            manager.itemBlocks.add(NbtUtils.readBlockPos((CompoundTag) t));
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // Pedestals
        ListTag pedestalList = new ListTag();
        for (BlockPos pos : pedestals) {
            pedestalList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("Pedestals", pedestalList);
        ListTag pendingList = new ListTag();
        for (BlockPos pos : pendingPedestalClears) pendingList.add(NbtUtils.writeBlockPos(pos));
        tag.put("PendingPedestalClears", pendingList);
        ListTag updateList = new ListTag();
        pendingPedestalUpdates.forEach((pos, update) -> {
            CompoundTag entry = new CompoundTag();
            entry.put("Position", NbtUtils.writeBlockPos(pos));
            if (update.locked() != null) entry.putBoolean("Locked", update.locked());
            if (update.linkedPositions() != null) entry.put("Links", writePositions(update.linkedPositions()));
            entry.put("RemovedLinks", writePositions(update.removedLinks()));
            updateList.add(entry);
        });
        tag.put("PendingPedestalUpdates", updateList);

        // Identifiers
        ListTag identifierList = new ListTag();
        for (Map.Entry<ResourceLocation, Set<BlockPos>> entry : identifiers.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("Type", entry.getKey().toString());

            ListTag posList = new ListTag();
            for (BlockPos pos : entry.getValue()) {
                posList.add(NbtUtils.writeBlockPos(pos));
            }
            entryTag.put("Positions", posList);
            identifierList.add(entryTag);
        }
        tag.put("Identifiers", identifierList);

        // ItemBlocks
        ListTag itemList = new ListTag();
        for (BlockPos pos : itemBlocks) {
            itemList.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("ItemBlocks", itemList);

        return tag;
    }

    // --------------------------
    // 获取实例
    // --------------------------
    private static Set<BlockPos> readPositions(ListTag list) {
        Set<BlockPos> positions = new HashSet<>();
        for (Tag entry : list) positions.add(NbtUtils.readBlockPos((CompoundTag) entry));
        return positions;
    }

    private static ListTag writePositions(Set<BlockPos> positions) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) list.add(NbtUtils.writeBlockPos(pos));
        return list;
    }

    public static BlockData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                BlockData::load,
                BlockData::new,
                "isaac_block_manager"
        );
    }
}
