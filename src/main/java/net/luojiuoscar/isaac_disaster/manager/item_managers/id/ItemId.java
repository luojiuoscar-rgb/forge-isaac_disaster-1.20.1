package net.luojiuoscar.isaac_disaster.manager.item_managers.id;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public enum ItemId {
    BREAKFAST(1),
    DESSERT(1),
    DINNER(1),
    LUNCH(1),
    SUPPER(1),
    MIDNIGHT_SNACK(1),
    ROTTEN_MEAT(1),
    A_SNACK(1),
    WOODEN_SPOON(1),
    STEVEN(3),
    CRICKETS_HEAD(4),
    THE_COMMON_COLD(1),
    GLASS_EYE(2),
    YUM_HEART(1),
    CAR_BATTERY(3),
    THE_BATTERY(2),
    VOLT_9(2),
    VOLT_4P5(2),
    THE_BOOK_OF_BELIAL(2),
    BOOM(0),
    MR_MEGA(3),
    BOMBER_BOY(3),
    SCATTER_BOMB(2),
    FAST_BOMB(2),
    BOBBY_BOMB(3),
    HOT_BOMB(2),
    BOOK_OF_SHADOW(3),
    TRANSCENDENCE(3),
    THE_BIBLE(2),
    BLOOD_OF_THE_MARTYR(3),
    THE_NECRONMICON(2),
    HOLY_MANTLE(4),
    WOODEN_NICKEL(1),
    THE_WAFER(4),
    MONEY_IS_POWER(3),
    DEAD_DOVE(3),
    CUPIDS_ARROW(2),
    SPOON_BENDER(3),
    ROID_RAGE(2),
    THE_SAD_ONION(3),
    WIRE_COAT_HANGER(3),
    SPEED_BALL(2),
    PISCES(2),
    MINI_MUSH(2),
    TELEPORT(1),
    LEMON_MISHAP(1),
    FREE_LEMONADE(1),
    THE_GAMEKID(1),
    UNICORN_STUMP(1),
    PHD(2),
    FALSE_PHD(2),
    A_QUARTER(0),
    A_DOLLAR(0),
    THE_INNER_EYE(2),
    PERFECT_VISION(4),
    MUTANT_SPIDER(3),
    POLYPHEMUS(4),
    HEART(2),
    RAW_LIVER(2),
    THE_BODY(2),
    GROWTH_HORMONES(3),
    SYNTHOIL(3),
    EXPERIMENTAL_TREATMENT(2),
    TORN_PHOTO(3),
    CAFFEINE_PILL(1),
    SAFETY_PIN(1),
    MY_LITTLE_UNICORN(1),
    MAGIC_MUSHROOM(4),
    BLUE_CAP(3),
    PLACEBO(2),
    DIPLOPIA(4),
    CROOKED_PENNY(2),
    DULL_RAZOR(1),
    HABIT(2),
    RUBBER_CEMENT(3),
    HOST_HAT(3),
    PYROMANIAC(4),
    PYRO(2),
    PIGGY_BANK(2),
    TINY_PLANET(0),
    MAGIC_SCAB(1),
    SCREW(3),
    BLACK_CANDLE(3),
    TAROT_CLOTH(2),
    TELEPATHY_FOR_DUMMIES(1),
    WHORE_OF_BABYLON(2),
    CURSE_OF_THE_TOWER(0),
    ANARCHIST_COOKBOOK(1),
    SMELTER(2),
    THE_SOUL(2),
    SACRED_ORB(4),
    SACK_HEAD(3),
    MITRE(2),
    THE_D6(4),
    GLITCHED_CROWN(4),
    BINGE_EATER(4),
    ECHO_CHAMBER(3),
    CHAOS(3),
    KAMIKAZE(1),
    TOOTH_PICKS(3),
    TECHNOLOGY(3),
    MARKED(1),
    THE_WIZ(0),
    MY_REFLECTION(2),
    IPECAC(4),
    BRIMSTONE(4),
    C_SECTION(4);



    private final int id;
    private final int level;
    private static final Map<Integer, RegistryObject<Item>> ID_TO_ITEM = new HashMap<>();

    ItemId(int level) {
        this.id = ordinal();
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public static void registerItem(int itemId, RegistryObject<Item> regItem) {
        ID_TO_ITEM.put(itemId, regItem);
    }

    public static RegistryObject<Item> getItemById(int id) {
        return ID_TO_ITEM.get(id);
    }
}
