package net.luojiuoscar.isaac_disaster.client;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 客户端专用的数据缓存类，存储从服务端同步过来的信息
public class ClientDataManager {
    private ClientDataManager() {
        itemCountMap = new HashMap<>();
        trinketCountMap = new HashMap<>();
        setCountMap = new HashMap<>();
        pillRecords = new HashMap<>();
        rockBottomHistory = new HashMap<>();
        reviveHudIcons = new ArrayList<>();
        init();
    }

    private static final ClientDataManager INSTANCE = new ClientDataManager();

    private final Map<ResourceLocation, Integer> itemCountMap;
    private final Map<ResourceLocation, Integer> trinketCountMap;
    private final Map<Integer, Integer> setCountMap;
    private final Map<Integer, ResourceLocation> pillRecords;
    private final Map<ResourceLocation, Double> rockBottomHistory;
    private final List<ResourceLocation> reviveHudIcons;
    private int flyUnits;
    private int pillQuality;

    private final Map<ResourceLocation, Float> chargeBars = new HashMap<>();

    public void init() {
        itemCountMap.clear();
        trinketCountMap.clear();
        setCountMap.clear();
        pillRecords.clear();
        rockBottomHistory.clear();
        reviveHudIcons.clear();
        pillQuality = 0;
        flyUnits = 0;
        chargeBars.clear();
    }

    public static ClientDataManager getInstance() {
        return INSTANCE;
    }

    /**
     * GETTER
     */
    public int getItemCount(ResourceLocation id) {
        return itemCountMap.getOrDefault(id, 0);
    }

    public void resetItemCountMap(){
        itemCountMap.clear();
    }

    public int getTrinketCount(ResourceLocation id) {
        return trinketCountMap.getOrDefault(id, 0);
    }

    public void resetTrinketCountMap() {
        trinketCountMap.clear();
    }

    public void resetSetCountMap(){
        setCountMap.clear();
    }

    public int getFlyUnits() {
        return flyUnits;
    }

    public int getSetCountFromId(int id){
        return setCountMap.getOrDefault(id, 0);
    }

    public boolean isPillRecordCorrectly(int pillId) {
        if (pillRecords.containsKey(pillId)){
            ResourceLocation correctEffect = PillEffectManager.getInstance().getEffectIdFromPill(pillId).getId();
            return pillRecords.get(pillId).equals(correctEffect);
        }
        return false;
    }
    public int getPillQuality(){
        return pillQuality;
    }

    /** Visible indicators only; zero progress may still be visible. */
    public Map<ResourceLocation, Float> getChargeBars() {
        return Map.copyOf(chargeBars);
    }

    public Double getRockBottomHistory(ResourceLocation key) {
        return rockBottomHistory.get(key);
    }

    public List<ResourceLocation> getReviveHudIcons() {
        return List.copyOf(reviveHudIcons);
    }

    public void updateChargeBar(ResourceLocation id, boolean visible, float progress) {
        if (id == null) {
            IsaacDisaster.LOGGER.warn("Skipping charge bar update with no registry ID");
            return;
        }
        if (!visible || !Float.isFinite(progress)) {
            chargeBars.remove(id);
        } else {
            chargeBars.put(id, Math.max(0f, Math.min(1f, progress)));
        }
    }

    /**
     * SETTER
     */
    public void setItemCount(ResourceLocation id, int count) {
        if (count <= 0) {
            itemCountMap.remove(id);
        } else {
            itemCountMap.put(id, count);
        }
    }
    public void setTrinketCount(ResourceLocation id, int count) {
        if (count <= 0) {
            trinketCountMap.remove(id);
        } else {
            trinketCountMap.put(id, count);
        }
    }

    public void setFlyPercentage(int flyUnits) {
        this.flyUnits = flyUnits;
    }
    public void setSetCountWithId(int id, int count){
        setCountMap.put(id, count);
    }
    public void setPillRecordsWithId(int pillId, ResourceLocation effectId){
        pillRecords.put(pillId, effectId);
    }
    public void setPillQuality(int pillQuality){
        this.pillQuality = pillQuality;
    }

    public void replaceRockBottomHistory(Map<ResourceLocation, Double> history) {
        rockBottomHistory.clear();
        rockBottomHistory.putAll(history);
    }

    public void replaceReviveHudIcons(List<ResourceLocation> icons) {
        reviveHudIcons.clear();
        reviveHudIcons.addAll(icons);
    }
}
