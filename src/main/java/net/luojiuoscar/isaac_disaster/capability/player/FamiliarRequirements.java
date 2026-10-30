package net.luojiuoscar.isaac_disaster.capability.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Persistent source contributions in registration order; producer-specific generation rules stay with producers. */
public final class FamiliarRequirements {
    private final Map<ResourceLocation, Integer> counts = new LinkedHashMap<>();

    public int getCount(ResourceLocation source) { return counts.getOrDefault(source, 0); }

    public int getTotal() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<ResourceLocation, Integer> getSources() { return Collections.unmodifiableMap(counts); }

    public void setCount(ResourceLocation source, int count) {
        Objects.requireNonNull(source);
        int other = getTotal() - getCount(source);
        int normalized = Math.max(0, Math.min(Integer.MAX_VALUE - other, count));
        if (normalized == 0) counts.remove(source);
        else counts.put(source, normalized);
    }

    public void addCount(ResourceLocation source, int delta) {
        setCount(source, (int) Math.max(0L, Math.min(Integer.MAX_VALUE, (long) getCount(source) + delta)));
    }

    /** Remove the preferred source first, then newest remaining contributions. */
    public void removeCount(ResourceLocation preferred, int count) {
        int remaining = Math.max(0, count);
        int removed = Math.min(remaining, getCount(preferred));
        addCount(preferred, -removed);
        remaining -= removed;
        var sources = new ArrayList<>(counts.keySet());
        for (int i = sources.size() - 1; i >= 0 && remaining > 0; i--) {
            ResourceLocation source = sources.get(i);
            removed = Math.min(remaining, getCount(source));
            addCount(source, -removed);
            remaining -= removed;
        }
    }

    void trimTo(int total) {
        var sources = new ArrayList<>(counts.keySet());
        if (!sources.isEmpty()) removeCount(sources.get(sources.size() - 1), Math.max(0, getTotal() - Math.max(0, total)));
    }

    public void clear() { counts.clear(); }

    ListTag saveNBT() {
        ListTag list = new ListTag();
        counts.forEach((source, count) -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("Source", source.toString());
            tag.putInt("Count", count);
            list.add(tag);
        });
        return list;
    }

    void loadNBT(ListTag list) {
        counts.clear();
        for (Tag value : list) {
            if (value instanceof CompoundTag tag) {
                ResourceLocation source = ResourceLocation.tryParse(tag.getString("Source"));
                if (source != null) addCount(source, tag.getInt("Count"));
            }
        }
    }
}
