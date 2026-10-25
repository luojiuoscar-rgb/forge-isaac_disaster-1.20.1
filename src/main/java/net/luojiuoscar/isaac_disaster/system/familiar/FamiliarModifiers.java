package net.luojiuoscar.isaac_disaster.system.familiar;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry.Parameter;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.ModFamiliarModifiers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Player-owned source counts, resolved against registered definitions at evaluation time. */
public final class FamiliarModifiers {
    private final Map<Parameter, Map<ResourceLocation, Integer>> entries = new EnumMap<>(Parameter.class);
    private final Function<ResourceLocation, FamiliarModifierEntry> definitions;

    public FamiliarModifiers() {
        this(ModFamiliarModifiers::get);
    }

    public FamiliarModifiers(Function<ResourceLocation, FamiliarModifierEntry> definitions) {
        this.definitions = Objects.requireNonNull(definitions, "familiar modifier definitions");
        for (Parameter parameter : Parameter.values()) entries.put(parameter, new LinkedHashMap<>());
    }

    public void clear() {
        entries.values().forEach(Map::clear);
    }

    public Map<ResourceLocation, Integer> snapshot(Parameter parameter) {
        return Map.copyOf(entries.get(parameter));
    }

    public int getCount(ResourceLocation id) {
        return entries.values().stream().mapToInt(group -> group.getOrDefault(id, 0)).sum();
    }

    public void add(ResourceLocation id, int count) {
        if (count <= 0) return;
        setCount(id, (int) Math.min(Integer.MAX_VALUE, (long) getCount(id) + count));
    }

    public void remove(ResourceLocation id, int count) {
        if (count <= 0) return;
        setCount(id, Math.max(0, getCount(id) - count));
    }

    public void setCount(ResourceLocation id, int count) {
        FamiliarModifierEntry definition = id == null ? null : definitions.apply(id);
        if (definition == null) {
            IsaacDisaster.LOGGER.warn("Ignoring unknown familiar modifier {}", id);
            return;
        }
        entries.values().forEach(group -> group.remove(id));
        if (count > 0) entries.get(definition.parameter()).put(id, count);
    }

    public double enhancement(AbstractIsaacFamiliarEntity familiar, boolean allowStacking) {
        return sum(Parameter.ENHANCEMENT, familiar, allowStacking);
    }

    public float visualScale(AbstractIsaacFamiliarEntity familiar, boolean allowStacking) {
        return (float) Math.max(0, Math.min(Float.MAX_VALUE, 1 + sum(Parameter.VISUAL_SCALE, familiar, allowStacking)));
    }

    private double sum(Parameter parameter, AbstractIsaacFamiliarEntity familiar, boolean allowStacking) {
        double result = 0;
        for (var held : entries.get(parameter).entrySet()) {
            FamiliarModifierEntry definition = definitions.apply(held.getKey());
            if (definition == null || definition.parameter() != parameter || !definition.filter().test(familiar)) continue;
            double amount = definition.amount() * (allowStacking ? held.getValue() : 1);
            double next = result + amount;
            result = Double.isFinite(next) ? next : Math.copySign(Double.MAX_VALUE, next);
        }
        return result;
    }

    public void copyFrom(FamiliarModifiers source) {
        if (source == this) return;
        clear();
        for (Parameter parameter : Parameter.values()) entries.get(parameter).putAll(source.entries.get(parameter));
    }

    public CompoundTag save() {
        CompoundTag result = new CompoundTag();
        for (Parameter parameter : Parameter.values()) {
            ListTag list = new ListTag();
            entries.get(parameter).forEach((id, count) -> {
                CompoundTag entry = new CompoundTag();
                entry.putString("id", id.toString());
                entry.putInt("count", count);
                list.add(entry);
            });
            result.put(key(parameter), list);
        }
        return result;
    }

    public void load(CompoundTag data) {
        clear();
        for (Parameter parameter : Parameter.values()) {
            for (Tag value : data.getList(key(parameter), Tag.TAG_COMPOUND)) {
                CompoundTag entry = (CompoundTag) value;
                ResourceLocation id = ResourceLocation.tryParse(entry.getString("id"));
                FamiliarModifierEntry definition = id == null ? null : definitions.apply(id);
                if (definition == null || definition.parameter() != parameter) {
                    IsaacDisaster.LOGGER.warn("Ignoring unknown or misplaced familiar modifier {} in {}", id, parameter);
                    continue;
                }
                setCount(id, entry.getInt("count"));
            }
        }
    }

    private static String key(Parameter parameter) {
        return parameter == Parameter.ENHANCEMENT ? "enhancement" : "visual_scale";
    }
}
