package net.luojiuoscar.isaac_disaster.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.HashMap;
import java.util.Map;

@AutoRegisterCapability
public class EntityEffect {
    private Map<Integer, Double> sourceDamageMap;

    public EntityEffect() {
        init();
    }

    public void init() {
        this.sourceDamageMap = new HashMap<>();
    }

    public double getSourceDamageFromId(int id) {
        return sourceDamageMap.getOrDefault(id, 0.0);
    }

    public void setSourceDamage(int id, double amount) {
        sourceDamageMap.put(id, amount);
    }

    // 从目标处复制
    public void copyFrom(EntityEffect source) {
        this.sourceDamageMap = new HashMap<>(source.sourceDamageMap);
    }

    // 保存到 NBT
    public void saveNBTData(CompoundTag nbt) {
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Integer, Double> entry : sourceDamageMap.entrySet()) {
            mapTag.putDouble(String.valueOf(entry.getKey()), entry.getValue());
        }
        nbt.put("source_damage_map", mapTag);
    }

    // 从 NBT 加载
    public void loadNBTData(CompoundTag nbt) {
        sourceDamageMap.clear();
        if (nbt.contains("source_damage_map", CompoundTag.TAG_COMPOUND)) {
            CompoundTag mapTag = nbt.getCompound("source_damage_map");
            for (String key : mapTag.getAllKeys()) {
                if (mapTag.contains(key, CompoundTag.TAG_DOUBLE)) {
                    double value = mapTag.getDouble(key);
                    sourceDamageMap.put(Integer.parseInt(key), value);
                }
            }
        }
    }
}
