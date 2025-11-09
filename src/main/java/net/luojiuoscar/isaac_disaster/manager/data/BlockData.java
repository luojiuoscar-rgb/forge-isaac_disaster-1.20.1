package net.luojiuoscar.isaac_disaster.manager.data;

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
    private final Map<ResourceLocation, Set<BlockPos>> identifiers = new HashMap<>();
    private final Set<BlockPos> itemBlocks = new HashSet<>(); // block with item display

    // --------------------------
    // Pedestal 操作
    // --------------------------
    public void addPedestal(BlockPos pos) {
        pedestals.add(pos.immutable());
        setDirty();
    }

    public void removePedestal(BlockPos pos) {
        if (pedestals.remove(pos)) {
            setDirty();
        }
    }

    public Set<BlockPos> getAllPedestals() {
        return new HashSet<>(pedestals);
    }

    // --------------------------
    // ItemBlock 操作
    // --------------------------
    public void addItemBlock(BlockPos pos) {
        itemBlocks.add(pos.immutable());
        setDirty();
    }

    public void removeItemBlock(BlockPos pos) {
        if (itemBlocks.remove(pos)) {
            setDirty();
        }
    }

    public Set<BlockPos> getAllItemBlocks() {
        return new HashSet<>(itemBlocks);
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

        // Identifiers
        ListTag identifierList = tag.getList("Identifiers", Tag.TAG_COMPOUND);
        for (Tag t : identifierList) {
            CompoundTag entry = (CompoundTag) t;
            ResourceLocation id = ResourceLocation.parse(entry.getString("Type"));

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
    public static BlockData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                BlockData::load,
                BlockData::new,
                "isaac_block_manager"
        );
    }
}
