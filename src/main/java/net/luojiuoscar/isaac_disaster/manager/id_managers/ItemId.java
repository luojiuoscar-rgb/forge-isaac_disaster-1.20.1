package net.luojiuoscar.isaac_disaster.manager.id_managers;

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
    A_DOLLAR;


    private final int id;

    // 构造方法：自动生成递增的ID
    ItemId() {
        this.id = ordinal();
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }
}
