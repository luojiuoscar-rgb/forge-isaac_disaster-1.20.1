package net.luojiuoscar.isaac_disaster.manager.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerItemPoolsData extends SavedData {

    private final Map<ResourceLocation, Set<ResourceLocation>> removeFromPool = new HashMap<>();
    private final Map<ResourceLocation, Set<ResourceLocation>> addFromPool = new HashMap<>();
    private final Set<ResourceLocation> removeAll = new HashSet<>();
    private final Set<ResourceLocation> addAll = new HashSet<>();

    // === 工具方法 ===
    public void clear(){
        removeFromPool.clear();
        addFromPool.clear();
        removeAll.clear();
        addAll.clear();
    }


    public void removeFromPool(ResourceLocation rl, ResourceLocation itemId) {
        removeFromPool.computeIfAbsent(rl, k -> new HashSet<>()).add(itemId);
        setDirty();
    }

    public void addToPool(ResourceLocation rl, ResourceLocation itemId) {
        addFromPool.computeIfAbsent(rl, k -> new HashSet<>()).add(itemId);
        setDirty();
    }

    public void removeFromAll(ResourceLocation itemId) {
        removeAll.add(itemId);
        setDirty();
    }

    public void addToAll(ResourceLocation itemId) {
        addAll.add(itemId);
        setDirty();
    }

    public Set<ResourceLocation> getRemoval(ResourceLocation rl){
        Set<ResourceLocation> ids = new HashSet<>(removeFromPool.getOrDefault(rl, Set.of()));
        ids.addAll(removeAll);
        return ids;
    }

    public Set<ResourceLocation> getAddition(ResourceLocation rl){
        Set<ResourceLocation> ids = new HashSet<>(addFromPool.getOrDefault(rl, Set.of()));
        ids.addAll(addAll);
        return ids;
    }

    public boolean isRemoved(ResourceLocation rl, ResourceLocation itemId) {
        return removeAll.contains(itemId)
                || removeFromPool.getOrDefault(rl, Set.of()).contains(itemId);
    }

    public boolean isAdded(ResourceLocation rl, ResourceLocation itemId) {
        return addAll.contains(itemId)
                || addFromPool.getOrDefault(rl, Set.of()).contains(itemId);
    }

    // === 保存 ===
    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("RemoveFromPool", writePool(removeFromPool));
        nbt.put("AddFromPool", writePool(addFromPool));
        nbt.put("RemoveAll", writeResourceLocationSet(removeAll));
        nbt.put("AddAll", writeResourceLocationSet(addAll));
        return nbt;
    }

    // === 读取 ===
    public static ServerItemPoolsData load(CompoundTag nbt) {
        ServerItemPoolsData data = new ServerItemPoolsData();

        if (nbt.contains("RemoveFromPool")) {
            data.readPool(nbt.getList("RemoveFromPool", 10), data.removeFromPool);
        }
        if (nbt.contains("AddFromPool")) {
            data.readPool(nbt.getList("AddFromPool", 10), data.addFromPool);
        }
        if (nbt.contains("RemoveAll")) {
            data.removeAll.addAll(data.readResourceLocationSet(nbt.getList("RemoveAll", Tag.TAG_STRING)));
        }
        if (nbt.contains("AddAll")) {
            data.addAll.addAll(data.readResourceLocationSet(nbt.getList("AddAll", Tag.TAG_STRING)));
        }

        return data;
    }

    // === 工具函数 ===

    private ListTag writePool(Map<ResourceLocation, Set<ResourceLocation>> pool) {
        ListTag list = new ListTag();
        for (var entry : pool.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entry.getKey().toString());
            tag.put("values", writeResourceLocationSet(entry.getValue()));
            list.add(tag);
        }
        return list;
    }

    private void readPool(ListTag list, Map<ResourceLocation, Set<ResourceLocation>> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("id"));
            if (id != null) target.put(id, readResourceLocationSet(tag.getList("values", Tag.TAG_STRING)));
        }
    }

    private ListTag writeResourceLocationSet(Set<ResourceLocation> set) {
        ListTag list = new ListTag();
        for (ResourceLocation value : set) {
            list.add(StringTag.valueOf(value.toString()));
        }
        return list;
    }

    private Set<ResourceLocation> readResourceLocationSet(ListTag list) {
        Set<ResourceLocation> result = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) result.add(id);
        }
        return result;
    }

    // === 获取单例实例 ===

    public static ServerItemPoolsData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ServerItemPoolsData::load,
                ServerItemPoolsData::new,
                "isaac_server_item_pools"
        );
    }
}
