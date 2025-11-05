package net.luojiuoscar.isaac_disaster.manager.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

/**
 * 记录当前呈现道具的方块坐标
 */
public class IsaacItemBlockData extends SavedData {
    private final Set<BlockPos> itemBlocks = new HashSet<>();

    public void addItemBlock(BlockPos pos) {
        itemBlocks.add(pos.immutable());
        setDirty();
    }

    public void removeItemBlock(BlockPos pos) {
        if (itemBlocks.remove(pos)) {
            setDirty();
        }
    }

    public Set<BlockPos> getAll() {
        return new HashSet<>(itemBlocks);
    }

    public static IsaacItemBlockData load(CompoundTag tag) {
        IsaacItemBlockData manager = new IsaacItemBlockData();
        ListTag list = tag.getList("ItemBlocks", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            manager.itemBlocks.add(NbtUtils.readBlockPos((CompoundTag) t));
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (BlockPos pos : itemBlocks) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("ItemBlocks", list);
        return tag;
    }

    public static IsaacItemBlockData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                IsaacItemBlockData::load,
                IsaacItemBlockData::new,
                "isaac_item_blocks_manager"
        );
    }
}
