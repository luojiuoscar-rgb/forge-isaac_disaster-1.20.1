package net.luojiuoscar.isaac_disaster.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ExtraData {
    private final Map<ResourceLocation, Double> doubleValues;
    private final Set<ResourceLocation> freezeSources;
    private final VisualLayerSet visualLayers;

    public ExtraData() {
        doubleValues = new HashMap<>();
        freezeSources = new LinkedHashSet<>();
        visualLayers = new VisualLayerSet();
        init();
    }

    public void init() {
        doubleValues.clear();
    }

    public Double getDouble(ResourceLocation key) {
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

    public boolean addFreezeSource(ResourceLocation source) {
        return freezeSources.add(source);
    }

    public boolean removeFreezeSource(ResourceLocation source) {
        return freezeSources.remove(source);
    }

    public boolean hasFreezeSource(ResourceLocation source) {
        return freezeSources.contains(source);
    }

    public boolean isFrozen() {
        return !freezeSources.isEmpty();
    }

    public boolean addVisualLayer(ResourceLocation layer) {
        return visualLayers.add(layer);
    }

    public boolean removeVisualLayer(ResourceLocation layer) {
        return visualLayers.remove(layer);
    }

    public boolean hasVisualLayer(ResourceLocation layer) {
        return visualLayers.contains(layer);
    }

    public Set<ResourceLocation> getFreezeSources() {
        return Collections.unmodifiableSet(freezeSources);
    }

    public Set<ResourceLocation> getActiveVisualLayers() {
        return visualLayers.getActiveLayers();
    }

    public Set<ResourceLocation> getResolvedVisualLayers() {
        return visualLayers.getResolvedLayers();
    }

    public void replaceRuntimeState(java.util.Collection<ResourceLocation> sources,
                                    java.util.Collection<ResourceLocation> layers) {
        freezeSources.clear();
        freezeSources.addAll(sources);
        visualLayers.replaceAll(layers);
    }

    public void copyFrom(ExtraData source) {
        this.doubleValues.clear();
        this.doubleValues.putAll(source.doubleValues);
        this.freezeSources.clear();
        this.visualLayers.clear();
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
