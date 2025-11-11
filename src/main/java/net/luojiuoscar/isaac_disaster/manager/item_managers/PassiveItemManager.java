package net.luojiuoscar.isaac_disaster.manager.item_managers;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.items.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 被动道具管理器，负责注册和管理所有道具实例
 */
public class  PassiveItemManager {
    // 单例实例
    private static final PassiveItemManager INSTANCE = new PassiveItemManager();
    // 存储道具ID与道具实例的映射
    private final Map<Integer, IPassiveItem> registeredItems = new HashMap<>();

    private PassiveItemManager() {}

    public static PassiveItemManager getInstance() {
        return INSTANCE;
    }

    /**
     * 注册道具
     * @param item 要注册的道具实例
     */
    public void registerItem(IPassiveItem item) {
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
    public IPassiveItem getItemFromId(int itemId) {
        return registeredItems.get(itemId);
    }

    public void registerItems(IPassiveItem... items) {
        for (IPassiveItem item : items) {
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
                new Dinner(),
                new Volt9(),
                new Volt4p5(),
                new Boom(),
                new MrMega(),
                new BomberBoy(),
                new ScatterBomb(),
                new FastBomb(),
                new BobbyBomb(),
                new HotBomb(),
                new Transcendence(),
                new BloodOfTheMartyr(),
                new HolyMantle(),
                new TheWafer(),
                new MoneyIsPower(),
                new DeadDove(),
                new CupidsArrow(),
                new SpoonBender(),
                new RoidRage(),
                new TheSadOnion(),
                new WireCoatHanger(),
                new SpeedBall(),
                new Pisces(),
                new MiniMush(),
                new Phd(),
                new FalsePhd(),
                new AQuarter(),
                new ADollar(),
                new TheInnerEye(),
                new PerfectVision(),
                new MutantSpider(),
                new Polyphemus(),
                new TheBody(),
                new RawLiver(),
                new Heart(),
                new GrowthHormones(),
                new Synthoil(),
                new ExperimentalTreatment(),
                new TornPhoto(),
                new SafetyPin(),
                new CaffeinePill(),
                new MagicMushroom(),
                new BlueCap(),
                new Habit(),
                new RubberCement(),
                new HostHat(),
                new Pyromaniac(),
                new Pyro(),
                new PiggyBank(),
                new TinyPlanet(),
                new MagicScab(),
                new Screw(),
                new BlackCandle(),
                new TarotCloth(),
                new WhoreOfBabylon(),
                new CurseOfTheTower(),
                new TheSoul(),
                new SacredOrb(),
                new SackHead(),
                new Mitre(),
                new GlitchedCrown(),
                new BingeEater(),
                new EchoChamber(),
                new Chaos()
        );
    }
}