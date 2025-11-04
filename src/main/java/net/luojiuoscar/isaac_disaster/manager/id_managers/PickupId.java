package net.luojiuoscar.isaac_disaster.manager.id_managers;

public enum PickupId {
    BOMB,
    GIGA_BOMB,
    GOLDEN_BOMB,
    HALF_RED_HEART,
    RED_HEART,
    DOUBLE_RED_HEART,
    HALF_SOUL_HEART,
    SOUL_HEART,
    BLENDED_HEART,
    BLACK_HEART,
    BONE_HEART,
    ISAAC_HEAD,
    THE_FOOL,
    THE_MAGICIAN,
    THE_HIGH_PRIESTESS,
    THE_EMPRESS,
    THE_HIEROPHANT,
    THE_LOVERS,
    THE_CHARIOT,
    JUSTICE,
    THE_HANGED_MAN,
    DEATH,
    THE_DEVIL,
    THE_TOWER,
    THE_SUN,
    TWO_OF_CLUBS,
    TWO_OF_DIAMONDS,
    TWO_OF_HEARTS,
    ACE_OF_CLUBS,
    ACE_OF_DIAMONDS,
    ACE_OF_HEARTS,
    HOLY_CARD,
    QUEEN_OF_HEARTS,
    THE_MAGICIAN_R,
    THE_HIGH_PRIESTESS_R,
    THE_HIEROPHANT_R,
    THE_WORLD,
    THE_STARS,
    GRAB_BAG,
    BLACK_SACK,
    GOLDEN_HEART,
    ETERNAL_HEART,
    SMALL_BATTERY,
    BATTERY,
    MEGA_BATTERY,
    GOLDEN_BATTERY;


    private final int id;

    // 构造方法：自动生成递增的ID
    PickupId() {
        this.id = ordinal();
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }
}
