package net.luojiuoscar.isaac_disaster.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

// 客户端专用的数据缓存类，存储从服务端同步过来的信息
public class ClientDataManager {
    private static final ClientDataManager INSTANCE = new ClientDataManager();

    private Map<Integer, Integer> itemCountMap;
    private int flyUnits;
    private Map<Integer, Integer> setCountMap;

    // constructor
    private ClientDataManager() {
        itemCountMap = new HashMap<>();
        flyUnits = 0;
        setCountMap = new HashMap<>();
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

    /**
     * SETTER
     */
    public void setItemWithId(int itemId, int count) {
        itemCountMap.put(itemId, count);
    }
    public void setFlyPercentage(int flyUnits) {
        this.flyUnits = flyUnits;
    }
    public void setSetCountWithId(int setId, int count){
        setCountMap.put(setId, count);
    }


    // 重置数据
    public void reset() {
        itemCountMap = new HashMap<>();
        flyUnits = 0;
        setCountMap = new HashMap<>();
    }
}