package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AutoRegisterCapability
public class PlayerItemPools {

    private final Map<ResourceLocation, Set<Integer>> removeFromPool;
    private final Map<ResourceLocation, Set<Integer>> addFromPool;
    private final Set<Integer> addAll;
    private final Set<Integer> removeAll;


    public PlayerItemPools() {
        removeFromPool = new HashMap<>();
        addFromPool = new HashMap<>();
        addAll = new HashSet<>();
        removeAll = new HashSet<>();
        init();
    }

    public void init() {
        removeFromPool.clear();
        addFromPool.clear();
        addAll.clear();
        removeAll.clear();
    }


    public void removeFromPool(ResourceLocation rl, int itemId){
        removeFromPool.computeIfAbsent(rl, k -> new HashSet<>())
                .add(itemId);
    }

    public void addToPool(ResourceLocation rl, int itemId){
        addFromPool.computeIfAbsent(rl, k -> new HashSet<>())
                .add(itemId);
    }

    public void removeFromAll(int itemId){
        removeAll.add(itemId);
    }

    public void addToAll(int itemId){
        addAll.add(itemId);
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

    public boolean isRemoved(ResourceLocation rl ,int itemId){
        return getRemoval(rl).contains(itemId);
    }
    public boolean isAdded(ResourceLocation rl ,int itemId){
        return getAddition(rl).contains(itemId);
    }

    // =====================
    // 数据持久化部分
    // =====================

    public void saveNBTData(CompoundTag nbt) {
        nbt.put("RemoveFromPool", writePool(removeFromPool));
        nbt.put("AddFromPool", writePool(addFromPool));
        nbt.put("RemoveAll", writeIntSet(removeAll));
        nbt.put("AddAll", writeIntSet(addAll));
    }

    public void loadNBTData(CompoundTag nbt) {
        removeFromPool.clear();
        addFromPool.clear();
        removeAll.clear();
        addAll.clear();

        if (nbt.contains("RemoveFromPool")) {
            readPool(nbt.getList("RemoveFromPool", 10), removeFromPool);
        }
        if (nbt.contains("AddFromPool")) {
            readPool(nbt.getList("AddFromPool", 10), addFromPool);
        }
        if (nbt.contains("RemoveAll")) {
            removeAll.addAll(readIntSet(nbt.getList("RemoveAll", 3)));
        }
        if (nbt.contains("AddAll")) {
            addAll.addAll(readIntSet(nbt.getList("AddAll", 3)));
        }
    }

    public void copyFrom(PlayerItemPools source) {
        this.removeFromPool.clear();
        this.addFromPool.clear();
        this.removeAll.clear();
        this.addAll.clear();

        for (var e : source.removeFromPool.entrySet()) {
            this.removeFromPool.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        for (var e : source.addFromPool.entrySet()) {
            this.addFromPool.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        this.removeAll.addAll(source.removeAll);
        this.addAll.addAll(source.addAll);
    }

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

}
