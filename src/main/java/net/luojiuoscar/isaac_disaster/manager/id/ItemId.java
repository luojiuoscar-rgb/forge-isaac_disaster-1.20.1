package net.luojiuoscar.isaac_disaster.manager.id;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public enum ItemId {
    BREAKFAST,
    DESSERT,
    DINNER,
    LUNCH,
    SUPPER,
    MIDNIGHT_SNACK,
    ROTTEN_MEAT,
    A_SNACK,
    WOODEN_SPOON,
    STEVEN,
    CRICKETS_HEAD,
    THE_COMMON_COLD,
    GLASS_EYE,
    YUM_HEART,
    CAR_BATTERY,
    THE_BATTERY,
    VOLT_9,
    VOLT_4P5,
    THE_BOOK_OF_BELIAL,
    BOOM,
    MR_MEGA,
    BOMBER_BOY,
    SCATTER_BOMB,
    FAST_BOMB,
    BOBBY_BOMB,
    HOT_BOMB,
    BOOK_OF_SHADOW,
    TRANSCENDENCE,
    THE_BIBLE,
    BLOOD_OF_THE_MARTYR,
    THE_NECRONMICON,
    HOLY_MANTLE,
    WOODEN_NICKEL,
    THE_WAFER,
    MONEY_IS_POWER,
    DEAD_DOVE,
    CUPIDS_ARROW,
    SPOON_BENDER,
    ROID_RAGE,
    THE_SAD_ONION,
    WIRE_COAT_HANGER,
    SPEED_BALL,
    PISCES,
    MINI_MUSH,
    TELEPORT,
    LEMON_MISHAP,
    FREE_LEMONADE,
    THE_GAMEKID,
    UNICORN_STUMP,
    PHD,
    FALSE_PHD,
    A_QUARTER,
    A_DOLLAR,
    THE_INNER_EYE,
    PERFECT_VISION,
    MUTANT_SPIDER,
    POLYPHEMUS,
    HEART,
    RAW_LIVER,
    THE_BODY,
    GROWTH_HORMONES,
    SYNTHOIL,
    EXPERIMENTAL_TREATMENT,
    TORN_PHOTO,
    CAFFEINE_PILL,
    SAFETY_PIN,
    MY_LITTLE_UNICORN,
    MAGIC_MUSHROOM,
    BLUE_CAP,
    PLACEBO,
    DIPLOPIA,
    CROOKED_PENNY,
    DULL_RAZOR,
    HABIT,
    RUBBER_CEMENT,
    HOST_HAT,
    PYROMANIAC,
    PYRO,
    PIGGY_BANK,
    TINY_PLANET,
    MAGIC_SCAB,
    SCREW,
    BLACK_CANDLE,
    TAROT_CLOTH,
    TELEPATHY_FOR_DUMMIES,
    WHORE_OF_BABYLON,
    CURSE_OF_THE_TOWER,
    ANARCHIST_COOKBOOK,
    SMELTER,
    THE_SOUL,
    SACRED_ORB,
    SACK_HEAD,
    MITRE,
    THE_D6,
    GLITCHED_CROWN,
    BINGE_EATER,
    ECHO_CHAMBER,
    CHAOS,
    KAMIKAZE,
    TOOTH_PICKS;





    private final int id;
    private static final Map<Integer, RegistryObject<Item>> ID_TO_ITEM = new HashMap<>();

    // 构造方法：自动生成递增的ID
    ItemId() {
        this.id = ordinal();
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }

    public static void registerItem(int itemId, RegistryObject<Item> regItem) {
        ID_TO_ITEM.put(itemId, regItem);
    }

    public static RegistryObject<Item> getItemById(int id) {
        return ID_TO_ITEM.get(id);
    }
}
