package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.passive_item.PassiveItem;
import net.luojiuoscar.isaac_disaster.passive_item.items.Breakfast;
import net.luojiuoscar.isaac_disaster.passive_item.items.Dessert;

import java.util.HashMap;
import java.util.Map;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * 被动道具管理器，负责注册和管理所有被动道具实例
 */
public class ItemManager {
    // 单例实例
    private static final ItemManager INSTANCE = new ItemManager();
    // 存储道具ID与道具实例的映射
    private final Map<Integer, PassiveItem> registeredItems = new HashMap<>();

    private ItemManager() {}

    public static ItemManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册被动道具
     * @param item 要注册的被动道具实例
     */
    public void registerItem(PassiveItem item) {
        int itemId = item.getItemId();
        if (registeredItems.containsKey(itemId)) {
            throw new IllegalArgumentException("被动道具ID已存在: " + itemId);
        }
        registeredItems.put(itemId, item);
    }

    public void registerItems(PassiveItem... items) {
        for (PassiveItem item : items) {
            registerItem(item);
        }
    }

    /**
     * 通过ID获取道具实例
     * @param itemId 道具ID
     * @return 对应的道具实例，不存在则返回null
     */
    public PassiveItem getItemFromId(int itemId) {
        return registeredItems.get(itemId);
    }

    /**
     * 初始化注册所有被动道具
     * 应在Mod初始化时调用
     */
    public void init() {
        // 注册所有被动道具实例
        registerItems(
                new Breakfast(),
                new Dessert()
        );
    }
}