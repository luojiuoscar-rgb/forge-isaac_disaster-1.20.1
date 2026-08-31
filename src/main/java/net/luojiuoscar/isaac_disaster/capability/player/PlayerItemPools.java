package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AutoRegisterCapability
public class PlayerItemPools {

    private final Map<ResourceLocation, Set<ResourceLocation>> removeFromPool;
    private final Map<ResourceLocation, Set<ResourceLocation>> addFromPool;
    private final Set<ResourceLocation> addAll;
    private final Set<ResourceLocation> removeAll;


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


    public void removeFromPool(ResourceLocation rl, ResourceLocation itemId){
        removeFromPool.computeIfAbsent(rl, k -> new HashSet<>())
                .add(itemId);
    }

    public void addToPool(ResourceLocation rl, ResourceLocation itemId){
        addFromPool.computeIfAbsent(rl, k -> new HashSet<>())
                .add(itemId);
    }

    public void removeFromAll(ResourceLocation itemId){
        removeAll.add(itemId);
    }

    public void addToAll(ResourceLocation itemId){
        addAll.add(itemId);
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

    public boolean isRemoved(ResourceLocation rl, ResourceLocation itemId){
        return getRemoval(rl).contains(itemId);
    }
    public boolean isAdded(ResourceLocation rl, ResourceLocation itemId){
        return getAddition(rl).contains(itemId);
    }

    // =====================
    // 数据持久化部分
    // =====================

    public void saveNBTData(CompoundTag nbt) {
        nbt.put("RemoveFromPool", writePool(removeFromPool));
        nbt.put("AddFromPool", writePool(addFromPool));
        nbt.put("RemoveAll", writeResourceLocationSet(removeAll));
        nbt.put("AddAll", writeResourceLocationSet(addAll));
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
            removeAll.addAll(readResourceLocationSet(nbt.getList("RemoveAll", Tag.TAG_STRING)));
        }
        if (nbt.contains("AddAll")) {
            addAll.addAll(readResourceLocationSet(nbt.getList("AddAll", Tag.TAG_STRING)));
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

}
