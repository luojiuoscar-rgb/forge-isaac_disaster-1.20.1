package net.luojiuoscar.isaac_disaster.manager.id;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public enum TrinketId {
    SWALLOWED_PENNY(2),
    AAA_BATTERY(1),
    BROKEN_REMOTE(0),
    CARTRIDGE(1),
    LUCKY_ROCK(1),
    LUCKY_TOE(1),
    CANCER_TRINKET(3),
    BLIND_RAGE(3),
    PERFECTION(3),
    DAEMONS_TAIL(2),
    PAPER_CLIP(2),
    ACE_OF_SPADES_TRINKET(1),
    SAFETY_CAP(1),
    RUSTED_KEY(1),
    CHILDS_HEART(1),
    MATCH_STICK(1),
    POKER_CHIP(1),
    GILDED_KEY(2),
    THE_LEFT_HAND(2),
    WIGGLE_WORM(1),
    BRAIN_WORM(3),
    WHIP_WORM(1),
    TAPE_WORM(1),
    LAZY_WORM(1),
    RING_WORM(1),
    OUROBOROS_WORM(1),
    HOOK_WORM(1),
    BLOODY_PENNY(1),
    BURNT_PENNY(2),
    FLAT_PENNY(2),
    BLESSED_PENNY(2),
    CHARGED_PENNY(2),
    COUNTERFEIT_PENNY(2),
    BUTT_PENNY(0),
    CURSED_PENNY(0),
    PETRIFIED_POOP(1),
    CALLUS(1),
    BLACK_LIPSTICK(0),
    BIBLE_TRACT(0),
    SAFETY_SCISSORS(3);


    private final int id;
    private final int level;

    private static final Map<Integer, RegistryObject<Item>> ID_TO_TRINKET = new HashMap<>();

    // 构造方法：自动生成递增的ID
    TrinketId(int level) {
        this.id = ordinal();
        this.level = level;
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }

    public int getLevel(){
        return level;
    }

    public static void registerItem(int itemId, RegistryObject<Item> regItem) {
        ID_TO_TRINKET.put(itemId, regItem);
    }

    public static RegistryObject<Item> getItemById(int id) {
        return ID_TO_TRINKET.get(id);
    }
}
