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

    // constructor
    private ClientDataManager() {
        itemCountMap = new HashMap<>();
        flyUnits = 0;
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

    /**
     * SETTER
     */
    public void setItemWithId(int itemId, int count) {
        itemCountMap.put(itemId, count);
    }
    public void setFlyPercentage(int flyUnits) {
        this.flyUnits = flyUnits;
    }


    // 重置数据
    public void reset() {
        itemCountMap = new HashMap<>();
        flyUnits = 0;
    }
}