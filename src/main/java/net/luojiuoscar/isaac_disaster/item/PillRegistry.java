package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.pickup.GoldenPill;
import net.luojiuoscar.isaac_disaster.item.pickup.Pill;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PillRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static final Map<Integer, RegistryObject<Item>> PILL_MAP = new HashMap<>();
    public static final Map<Integer, RegistryObject<Item>> PILL_H_MAP = new HashMap<>();
    public static final Map<Boolean, RegistryObject<Item>> GOLDEN_PILL_MAP = new HashMap<>();

    private static final int MAX_PILL = 13;

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

        // 普通药丸
        for (int i = 1; i <= MAX_PILL; i++) {
            final int id = i;
            RegistryObject<Item> pill = ITEMS.register("pill" + id,
                    () -> new Pill(new Item.Properties(), id, false));
            ItemListManager.PICKUP_LIST.add(pill);
            PILL_MAP.put(id, pill);
        }

        // 堆叠药丸
        for (int i = 1; i <= MAX_PILL; i++) {
            final int id = i;
            RegistryObject<Item> pillH = ITEMS.register("pill" + id + "_h",
                    () -> new Pill(new Item.Properties().stacksTo(16), id, true));
            ItemListManager.PICKUP_LIST.add(pillH);
            PILL_H_MAP.put(id, pillH);
        }

        // 黄金药丸
        RegistryObject<Item> GOLDEN_PILL = ITEMS.register("golden_pill",
                () -> new GoldenPill(new Item.Properties(), false));
        RegistryObject<Item> GOLDEN_PILL_H = ITEMS.register("golden_pill_h",
                () -> new GoldenPill(new Item.Properties(), true));

        GOLDEN_PILL_MAP.put(false, GOLDEN_PILL);
        GOLDEN_PILL_MAP.put(true, GOLDEN_PILL_H);

        ItemListManager.PICKUP_LIST.add(GOLDEN_PILL);
        ItemListManager.PICKUP_LIST.add(GOLDEN_PILL_H);
    }

    public static RegistryObject<Item> getPillById(int id, boolean isHorse) {
        return isHorse ? PILL_H_MAP.get(id) : PILL_MAP.get(id);
    }

    public static RegistryObject<Item> getGoldenPill(boolean isHorse) {
        return GOLDEN_PILL_MAP.get(isHorse);
    }
}
