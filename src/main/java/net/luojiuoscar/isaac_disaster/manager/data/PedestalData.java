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

public class PedestalData extends SavedData {
    private final Set<BlockPos> pedestals = new HashSet<>();

    public void addPedestal(BlockPos pos) {
        pedestals.add(pos.immutable());
        setDirty();
    }

    public void removePedestal(BlockPos pos) {
        if (pedestals.remove(pos)) {
            setDirty();
        }
    }

    public Set<BlockPos> getAll() {
        return new HashSet<>(pedestals);
    }

    public static PedestalData load(CompoundTag tag) {
        PedestalData manager = new PedestalData();
        ListTag list = tag.getList("Pedestals", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            manager.pedestals.add(NbtUtils.readBlockPos((CompoundTag) t));
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (BlockPos pos : pedestals) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("Pedestals", list);
        return tag;
    }

    public static PedestalData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PedestalData::load,
                PedestalData::new,
                "isaac_pedestal_manager"
        );
    }
}
