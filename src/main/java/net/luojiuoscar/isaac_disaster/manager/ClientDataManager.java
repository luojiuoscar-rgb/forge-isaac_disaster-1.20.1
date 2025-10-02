package net.luojiuoscar.isaac_disaster.manager;

import java.util.HashMap;
import java.util.Map;

// 客户端专用的数据缓存类，存储从服务端同步过来的信息
public class ClientDataManager {
    private static final ClientDataManager INSTANCE = new ClientDataManager();

    private Map<Integer, Integer> itemCountMap;

    private ClientDataManager() {
        itemCountMap = new HashMap<>();
    }

    public static ClientDataManager getInstance() {
        return INSTANCE;
    }

    public int getCountFromId(int itemId) {
        return itemCountMap.getOrDefault(itemId, 0);
    }

    public void setItemWithId(int itemId, int count) {
        itemCountMap.put(itemId, count);
    }

    // 重置数据
    public void reset() {
        itemCountMap = new HashMap<>();
    }
}