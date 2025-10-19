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
    THE_HIEROPHANT;

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
