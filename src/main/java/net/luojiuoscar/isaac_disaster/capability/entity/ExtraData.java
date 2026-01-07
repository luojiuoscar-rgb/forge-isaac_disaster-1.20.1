package net.luojiuoscar.isaac_disaster.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ExtraData {
    private final Map<ResourceLocation, Double> doubleValues;

    public ExtraData() {
        doubleValues = new HashMap<>();
        init();
    }

    public void init() {
        doubleValues.clear();
    }

    public @Nullable Double getDouble(ResourceLocation key) {
        return doubleValues.getOrDefault(key, null);
    }

    public void setDouble(ResourceLocation key, double value) {
        doubleValues.put(key, value);
    }

    public boolean hasDouble(ResourceLocation key) {
        return doubleValues.containsKey(key);
    }

    public void removeDouble(ResourceLocation key) {
        doubleValues.remove(key);
    }

    public void copyFrom(ExtraData source) {
        this.doubleValues.clear();
        this.doubleValues.putAll(source.doubleValues);
    }

    public void saveNBTData(CompoundTag nbt) {
        CompoundTag doubleTag = new CompoundTag();
        for (Map.Entry<ResourceLocation, Double> entry : doubleValues.entrySet()) {
            // key 转成字符串存储
            doubleTag.putDouble(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("doubleValues", doubleTag);
    }

    public void loadNBTData(CompoundTag nbt) {
        doubleValues.clear();
        if (!nbt.contains("doubleValues")) return;

        CompoundTag doubleTag = nbt.getCompound("doubleValues");
        for (String keyStr : doubleTag.getAllKeys()) {
            double value = doubleTag.getDouble(keyStr);
            ResourceLocation key = ResourceLocation.tryParse(keyStr);
            if (key != null) {
                doubleValues.put(key, value);
            }
        }
    }
}

