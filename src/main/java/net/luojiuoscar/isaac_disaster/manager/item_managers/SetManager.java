package net.luojiuoscar.isaac_disaster.manager.item_managers;

import net.luojiuoscar.isaac_disaster.item_ability.set.ISet;
import net.luojiuoscar.isaac_disaster.item_ability.set.sets.Adult;
import net.luojiuoscar.isaac_disaster.item_ability.set.sets.Book;
import net.luojiuoscar.isaac_disaster.item_ability.set.sets.FunGuy;
import net.luojiuoscar.isaac_disaster.item_ability.set.sets.Spun;


import java.util.HashMap;
import java.util.Map;


public class SetManager {
    // 单例实例
    private static final SetManager INSTANCE = new SetManager();
    private SetManager() {}
    public static SetManager getInstance() {
        return INSTANCE;
    }

    private final Map<Integer, ISet> registeredItems = new HashMap<>();

    public void registerItem(ISet item) {
        int itemId = item.getSetId();
        if (registeredItems.containsKey(itemId)) {
            throw new IllegalArgumentException("ID已存在: " + itemId);
        }
        registeredItems.put(itemId, item);
    }

    /**
     * 通过ID获取道具实例
     * @param setId 道具ID
     * @return 对应的道具实例，不存在则返回null
     */
    public ISet getSetFromId(int setId) {
        return registeredItems.get(setId);
    }

    public void registerItems(ISet... items) {
        for (ISet item : items) {
            registerItem(item);
        }
    }


    public void init() {
        // 注册所有道具实例
        registerItems(
                new Spun(),
                new Adult(),
                new FunGuy(),
                new Book()
        );
    }
}