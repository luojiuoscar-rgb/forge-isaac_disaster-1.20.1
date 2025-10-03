package net.luojiuoscar.isaac_disaster.manager.item_managers;

import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPickup;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.items.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * 被动道具管理器，负责注册和管理所有道具实例
 */
public class PickupManager {
    // 单例实例
    private static final PickupManager INSTANCE = new PickupManager();
    private PickupManager() {}
    public static PickupManager getInstance() {
        return INSTANCE;
    }

    // 存储道具ID与道具实例的映射
    private final Map<Integer, IPickup> registeredItems = new HashMap<>();

    /**
     * 注册道具
     * @param item 要注册的道具实例
     */
    public void registerItem(IPickup item) {
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
    public IPickup getItemFromId(int itemId) {
        return registeredItems.get(itemId);
    }

    public void registerItems(IPickup... items) {
        for (IPickup item : items) {
            registerItem(item);
        }
    }


    public void init() {
        // 注册所有道具实例
        registerItems(
                new Bomb(),
                new GigaBomb(),
                new GoldenBomb()
        );
    }
}