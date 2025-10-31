package net.luojiuoscar.isaac_disaster.manager.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;


import java.util.HashMap;
import java.util.Map;

public class PillShuffleData extends SavedData {
    private static final String DATA_NAME = "pill_shuffle_data";

    private final Map<Integer, Integer> pillEffectMap = new HashMap<>();

    public static PillShuffleData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PillShuffleData::load,
                PillShuffleData::new,
                DATA_NAME
        );
    }

    public PillShuffleData() {}

    public static PillShuffleData load(CompoundTag tag) {
        PillShuffleData data = new PillShuffleData();
        CompoundTag mapTag = tag.getCompound("Map");
        for (String key : mapTag.getAllKeys()) {
            data.pillEffectMap.put(Integer.parseInt(key), mapTag.getInt(key));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag mapTag = new CompoundTag();
        for (Map.Entry<Integer, Integer> entry : pillEffectMap.entrySet()) {
            mapTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("Map", mapTag);
        return tag;
    }

    public Map<Integer, Integer> getPillEffectMap() {
        return pillEffectMap;
    }

    public void setMapping(int pillId, int effectId) {
        pillEffectMap.put(pillId, effectId);
        setDirty();
    }

    public void clear() {
        pillEffectMap.clear();
        setDirty();
    }
}
