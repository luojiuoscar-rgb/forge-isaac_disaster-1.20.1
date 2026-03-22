package net.luojiuoscar.isaac_disaster.client;

import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

// 客户端专用的数据缓存类，存储从服务端同步过来的信息
public class ClientDataManager {
    private static final ClientDataManager INSTANCE = new ClientDataManager();

    private final Map<Integer, Integer> itemCountMap;
    private final Map<Integer, Integer> setCountMap;
    private final Map<Integer, ResourceLocation> pillRecords;
    private int flyUnits;
    private int pillQuality;

    private float chargeProgress;

    // constructor
    private ClientDataManager() {
        itemCountMap = new HashMap<>();
        setCountMap = new HashMap<>();
        pillRecords = new HashMap<>();
        init();
    }

    public void init() {
        itemCountMap.clear();
        setCountMap.clear();
        pillRecords.clear();
        pillQuality = 0;
        flyUnits = 0;
        chargeProgress = 0;
    }

    public static ClientDataManager getInstance() {
        return INSTANCE;
    }

    /**
     * GETTER
     */
    public int getCountFromId(int id) {
        return itemCountMap.getOrDefault(id, 0);
    }

    public void resetItemCountMap(){
        itemCountMap.clear();
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

    public float getChargeProgress() {
        return chargeProgress;
    }

    public void setChargeProgress(float chargeProgress) {
        this.chargeProgress = chargeProgress;
    }

    /**
     * SETTER
     */
    public void setItemWithId(int id, int count) {
        itemCountMap.put(id, count);
    }
    public void modifyItemCount(int id, int count) {
        int c = itemCountMap.getOrDefault(id, 0) + count;
        if (c <= 0){
            itemCountMap.remove(id);
        }else{
            itemCountMap.put(id, c);
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
}