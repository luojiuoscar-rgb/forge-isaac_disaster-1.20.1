package net.luojiuoscar.isaac_disaster.manager;

import net.luojiuoscar.isaac_disaster.isaac.passive_item.PassiveItem;
import net.luojiuoscar.isaac_disaster.isaac.passive_item.items.*;

import java.util.*;

/**
 * 被动道具管理器，负责注册和管理所有道具实例
 */
public class PassiveItemManager {
    // 单例实例
    private static final PassiveItemManager INSTANCE = new PassiveItemManager();
    // 存储道具ID与道具实例的映射
    private final Map<Integer, PassiveItem> registeredItems = new HashMap<>();

    private PassiveItemManager() {}

    public static PassiveItemManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册道具
     * @param item 要注册的道具实例
     */
    public void registerItem(PassiveItem item) {
        int itemId = item.getItemId();
        if (registeredItems.containsKey(itemId)) {
            throw new IllegalArgumentException("被动道具ID已存在: " + itemId);
        }
        registeredItems.put(itemId, item);
    }

    /**
     * 通过ID获取道具实例
     * @param itemId 道具ID
     * @return 对应的道具实例，不存在则返回null
     */
    public PassiveItem getItemFromId(int itemId) {
        return registeredItems.get(itemId);
    }

    public void registerItems(PassiveItem... items) {
        for (PassiveItem item : items) {
            registerItem(item);
        }
    }


    public void init() {
        // 注册所有道具实例
        registerItems(
                new Breakfast(),
                new Dessert(),
                new WoodenSpoon(),
                new Steven(),
                new CricketsHead(),
                new TheCommonCold(),
                new GlassEye(),
                new CarBattery(),
                new TheBattery(),
                new Lunch(),
                new MidnightSnack(),
                new Supper(),
                new RottenMeat(),
                new ASnack(),
                new Dinner()
        );
    }
}