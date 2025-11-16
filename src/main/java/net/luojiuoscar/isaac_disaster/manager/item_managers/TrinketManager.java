package net.luojiuoscar.isaac_disaster.manager.item_managers;

import net.luojiuoscar.isaac_disaster.item_ability.trinket.ITrinket;
import net.luojiuoscar.isaac_disaster.item_ability.trinket.items.*;

import java.util.HashMap;
import java.util.Map;


public class TrinketManager {
    // 单例实例
    private static final TrinketManager INSTANCE = new TrinketManager();
    private TrinketManager() {}
    public static TrinketManager getInstance() {
        return INSTANCE;
    }

    private final Map<Integer, ITrinket> registeredItems = new HashMap<>();

    public void registerItem(ITrinket item) {
        int itemId = item.getId();
        if (registeredItems.containsKey(itemId)) {
            throw new IllegalArgumentException("ID已存在: " + itemId);
        }
        registeredItems.put(itemId, item);
    }

    public ITrinket getTrinketFromId(int itemId) {
        return registeredItems.get(itemId);
    }

    public void registerItems(ITrinket... items) {
        for (ITrinket item : items) {
            registerItem(item);
        }
    }


    public void init() {
        // 注册所有道具实例
        registerItems(
                new SwallowedPenny(),
                new AAABattery(),
                new BrokenRemote(),
                new Cartridge(),
                new LuckyToe(),
                new LuckyRock(),
                new CancerTrinket(),
                new BlindRage(),
                new Perfection(),
                new DaemonsTail(),
                new PaperClip(),
                new SafetyCap(),
                new AceOfSpadesTrinket(),
                new ChildsHeart(),
                new RustedKey(),
                new MatchStick(),
                new PokerChip(),
                new GildedKey(),
                new TheLeftHand(),
                new WiggleWorm(),
                new BrainWorm(),
                new WhipWorm(),
                new TapeWorm(),
                new LazyWorm(),
                new RingWorm(),
                new OuroborosWorm(),
                new HookWorm()
        );
    }
}