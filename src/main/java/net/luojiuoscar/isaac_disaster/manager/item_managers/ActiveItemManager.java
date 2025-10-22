package net.luojiuoscar.isaac_disaster.manager.item_managers;

import net.luojiuoscar.isaac_disaster.item_ability.active_item.IActiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.active_item.items.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 被动道具管理器，负责注册和管理所有道具实例
 */
public class ActiveItemManager {
    // 单例实例
    private static final ActiveItemManager INSTANCE = new ActiveItemManager();
    private ActiveItemManager() {}
    public static ActiveItemManager getInstance() {
        return INSTANCE;
    }

    // 存储道具ID与道具实例的映射
    private final Map<Integer, IActiveItem> registeredItems = new HashMap<>();

    /**
     * 注册道具
     * @param item 要注册的道具实例
     */
    public void registerItem(IActiveItem item) {
        int itemId = item.getItemId();
        if (registeredItems.containsKey(itemId)) {
            throw new IllegalArgumentException("道具ID已存在: " + itemId);
        }
        registeredItems.put(itemId, item);
    }

    /**
     * 通过ID获取道具实例
     * @param itemId 道具ID
     * @return 对应的道具实例，不存在则返回null
     */
    public IActiveItem getItemFromId(int itemId) {
        return registeredItems.get(itemId);
    }

    public void registerItems(IActiveItem... items) {
        for (IActiveItem item : items) {
            registerItem(item);
        }
    }


    public void init() {
        // 注册所有道具实例
        registerItems(
                new YumHeart(),
                new TheBookOfBelial(),
                new BookOfShadow(),
                new TheBible(),
                new TheNecronmicon(),
                new WoodenNickel(),
                new Teleport(),
                new LemonMishap(),
                new FreeLemonade(),
                new TheGamekid(),
                new UnicornStump(),
                new MyLittleUnicorn(),
                new Placebo(),
                new Diplopia(),
                new CrookedPenny(),
                new DullRazor(),
                new TelepathyForDummies(),
                new AnarchistCookbook(),
                new Smelter()
        );
    }
}