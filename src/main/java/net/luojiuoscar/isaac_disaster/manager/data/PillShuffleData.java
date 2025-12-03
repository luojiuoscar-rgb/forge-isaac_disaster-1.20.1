package net.luojiuoscar.isaac_disaster.manager.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class PillShuffleData extends SavedData {
    public PillShuffleData() {}

    private static final String DATA_NAME = "pill_shuffle_data";

    /** 存 pillId(int) → effectId(ResourceLocation) */
    private final Map<Integer, ResourceLocation> pillEffectMap = new HashMap<>();


    public static PillShuffleData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PillShuffleData::load,
                PillShuffleData::new,
                DATA_NAME
        );
    }

    public static PillShuffleData load(CompoundTag tag) {
        PillShuffleData data = new PillShuffleData();

        CompoundTag mapTag = tag.getCompound("Map");

        for (String key : mapTag.getAllKeys()) {
            int pillId = Integer.parseInt(key);
            String rlStr = mapTag.getString(key);
            ResourceLocation rl = ResourceLocation.parse(rlStr);

            data.pillEffectMap.put(pillId, rl);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag mapTag = new CompoundTag();

        for (Map.Entry<Integer, ResourceLocation> entry : pillEffectMap.entrySet()) {
            mapTag.putString(entry.getKey().toString(), entry.getValue().toString());
        }

        tag.put("Map", mapTag);
        return tag;
    }

    public Map<Integer, ResourceLocation> getPillEffectMap() {
        return pillEffectMap;
    }

    public void setMapping(int pillId, ResourceLocation effectId) {
        pillEffectMap.put(pillId, effectId);
        setDirty();
    }

    public void clear() {
        pillEffectMap.clear();
        setDirty();
    }
}
