package net.luojiuoscar.isaac_disaster.client;

import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;

import java.util.HashMap;
import java.util.Map;

// 客户端专用的数据缓存类，存储从服务端同步过来的信息
public class ClientDataManager {
    private static final ClientDataManager INSTANCE = new ClientDataManager();

    private final Map<Integer, Integer> itemCountMap;
    private final Map<Integer, Integer> setCountMap;
    private final Map<Integer, Integer> pillRecords;
    private int flyUnits;
    private int pillQuality;


    // constructor
    private ClientDataManager() {
        itemCountMap = new HashMap<>();
        flyUnits = 0;
        setCountMap = new HashMap<>();
        pillRecords = new HashMap<>();
        pillQuality = 0;
    }

    public static ClientDataManager getInstance() {
        return INSTANCE;
    }

    /**
     * GETTER
     */
    public int getCountFromId(int itemId) {
        return itemCountMap.getOrDefault(itemId, 0);
    }
    public int getFlyUnits() {
        return flyUnits;
    }
    public int getSetCountFromId(int setId){
        return setCountMap.getOrDefault(setId, 0);
    }
    public boolean isPillRecordCorrectly(int pillId) {
        if (pillRecords.containsKey(pillId)){
            int correctEffect = PillEffectManager.getInstance().getEffectIdFromPill(pillId);
            return pillRecords.get(pillId) == correctEffect;
        }
        return false;
    }
    public int getPillQuality(){
        return pillQuality;
    }


    /**
     * SETTER
     */
    public void setItemWithId(int itemId, int count) {
        itemCountMap.put(itemId, count);
    }
    public void modifyItemCount(int itemId, int count) {
        int c = itemCountMap.getOrDefault(itemId, 0) + count;
        if (c <= 0){
            itemCountMap.remove(itemId);
        }else{
            itemCountMap.put(itemId, c);
        }
    }
    public void setFlyPercentage(int flyUnits) {
        this.flyUnits = flyUnits;
    }
    public void setSetCountWithId(int setId, int count){
        setCountMap.put(setId, count);
    }
    public void setPillRecordsWithId(int pillId, int effectId){
        pillRecords.put(pillId, effectId);
    }
    public void setPillQuality(int pillQuality){
        this.pillQuality = pillQuality;
    }


    // 重置数据
    public void reset() {
        itemCountMap.clear();
        flyUnits = 0;
        setCountMap.clear();
        pillRecords.clear();
        pillQuality = 0;
    }
}