package net.luojiuoscar.isaac_disaster.manager.id;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public enum TrinketId {
    SWALLOWED_PENNY,
    AAA_BATTERY,
    BROKEN_REMOTE,
    CARTRIDGE,
    LUCKY_ROCK,
    LUCKY_TOE,
    CANCER_TRINKET,
    BLIND_RAGE,
    PERFECTION,
    DAEMONS_TAIL,
    PAPER_CLIP,
    ACE_OF_SPADES_TRINKET,
    SAFETY_CAP,
    RUSTED_KEY,
    CHILDS_HEART,
    MATCH_STICK,
    POKER_CHIP,
    GILDED_KEY,
    THE_LEFT_HAND;


    private final int id;
    private static final Map<Integer, RegistryObject<Item>> ID_TO_TRINKET = new HashMap<>();

    // 构造方法：自动生成递增的ID
    TrinketId() {
        this.id = ordinal();
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }

    public static void registerItem(int itemId, RegistryObject<Item> regItem) {
        ID_TO_TRINKET.put(itemId, regItem);
    }

    public static RegistryObject<Item> getItemById(int id) {
        return ID_TO_TRINKET.get(id);
    }
}
