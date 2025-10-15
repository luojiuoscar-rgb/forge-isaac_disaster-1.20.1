package net.luojiuoscar.isaac_disaster.capability.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.HashMap;
import java.util.Map;

@AutoRegisterCapability
public class EntityEffect {
    private Map<String, Double> sourceDamageMap;


    public EntityEffect(){
        init();
    }

    public void init(){
        this.sourceDamageMap = new HashMap<>();
    }

    public double getSourceDamageFromName(String name){
        return sourceDamageMap.getOrDefault(name, 0.0);
    }


    public void setSourceDamage(String name, double amount){
        sourceDamageMap.put(name, amount);
    }


    // 从目标处复制
    public void copyFrom(EntityEffect source) {
        this.sourceDamageMap = source.sourceDamageMap;
    }

    public void saveNBTData(CompoundTag nbt) {
        // 创建一个子 CompoundTag 用于存储 Map 的键值对
        CompoundTag mapTag = new CompoundTag();

        // 遍历 Map，将每个键值对存入子标签
        for (Map.Entry<String, Double> entry : sourceDamageMap.entrySet()) {
            String key = entry.getKey();
            double value = entry.getValue();
            mapTag.putDouble(key, value); // 若值是 float 则用 putFloat
        }

        // 将子标签存入主 NBT 标签
        nbt.put("source_damage_map", mapTag);
    }

    // 从 NBT 加载 Map 数据
    public void loadNBTData(CompoundTag nbt) {
        // 清空现有 Map，避免残留旧数据
        sourceDamageMap.clear();

        // 检查 NBT 中是否存在对应的子标签，且类型为 CompoundTag
        if (nbt.contains("source_damage_map", CompoundTag.TAG_COMPOUND)) {
            CompoundTag mapTag = nbt.getCompound("source_damage_map");

            // 遍历子标签的所有键，恢复到 Map 中
            for (String key : mapTag.getAllKeys()) {
                if (mapTag.contains(key, CompoundTag.TAG_DOUBLE)) { // 检查值类型是否匹配
                    double value = mapTag.getDouble(key); // 若值是 float 则用 getFloat
                    sourceDamageMap.put(key, value);
                }
            }
        }
    }
}