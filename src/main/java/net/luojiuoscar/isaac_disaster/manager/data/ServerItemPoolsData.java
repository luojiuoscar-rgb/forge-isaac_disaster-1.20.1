package net.luojiuoscar.isaac_disaster.manager.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerItemPoolsData extends SavedData {

    private final Map<ResourceLocation, Set<Integer>> removeFromPool = new HashMap<>();
    private final Map<ResourceLocation, Set<Integer>> addFromPool = new HashMap<>();
    private final Set<Integer> removeAll = new HashSet<>();
    private final Set<Integer> addAll = new HashSet<>();

    // === 工具方法 ===
    public void clear(){
        removeFromPool.clear();
        addFromPool.clear();
        removeAll.clear();
        addAll.clear();
    }


    public void removeFromPool(ResourceLocation rl, int itemId) {
        removeFromPool.computeIfAbsent(rl, k -> new HashSet<>()).add(itemId);
        setDirty();
    }

    public void addToPool(ResourceLocation rl, int itemId) {
        addFromPool.computeIfAbsent(rl, k -> new HashSet<>()).add(itemId);
        setDirty();
    }

    public void removeFromAll(int itemId) {
        removeAll.add(itemId);
        setDirty();
    }

    public void addToAll(int itemId) {
        addAll.add(itemId);
        setDirty();
    }

    public Set<Integer> getRemoval(ResourceLocation rl){
        Set<Integer> ids = removeFromPool.getOrDefault(rl, new HashSet<>());
        ids.addAll(removeAll);
        return ids;
    }

    public Set<Integer> getAddition(ResourceLocation rl){
        Set<Integer> ids = addFromPool.getOrDefault(rl, new HashSet<>());
        ids.addAll(addAll);
        return ids;
    }

    public boolean isRemoved(ResourceLocation rl, int itemId) {
        return removeAll.contains(itemId)
                || removeFromPool.getOrDefault(rl, Set.of()).contains(itemId);
    }

    public boolean isAdded(ResourceLocation rl, int itemId) {
        return addAll.contains(itemId)
                || addFromPool.getOrDefault(rl, Set.of()).contains(itemId);
    }

    // === 保存 ===
    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.put("RemoveFromPool", writePool(removeFromPool));
        nbt.put("AddFromPool", writePool(addFromPool));
        nbt.put("RemoveAll", writeIntSet(removeAll));
        nbt.put("AddAll", writeIntSet(addAll));
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
            data.removeAll.addAll(data.readIntSet(nbt.getList("RemoveAll", 3)));
        }
        if (nbt.contains("AddAll")) {
            data.addAll.addAll(data.readIntSet(nbt.getList("AddAll", 3)));
        }

        return data;
    }

    // === 工具函数 ===

    private ListTag writePool(Map<ResourceLocation, Set<Integer>> pool) {
        ListTag list = new ListTag();
        for (var entry : pool.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", entry.getKey().toString());
            tag.put("values", writeIntSet(entry.getValue()));
            list.add(tag);
        }
        return list;
    }

    private void readPool(ListTag list, Map<ResourceLocation, Set<Integer>> target) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            ResourceLocation id = ResourceLocation.parse(tag.getString("id"));
            Set<Integer> values = readIntSet(tag.getList("values", 3));
            target.put(id, values);
        }
    }

    private ListTag writeIntSet(Set<Integer> set) {
        ListTag list = new ListTag();
        for (int val : set) {
            list.add(IntTag.valueOf(val));
        }
        return list;
    }

    private Set<Integer> readIntSet(ListTag list) {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.getInt(i));
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
