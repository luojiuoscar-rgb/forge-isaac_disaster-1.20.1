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
    BLACK_HEART;


    private final int id;

    // 构造方法：自动生成递增的ID
    PickupId() {
        this.id = ordinal() + 1; // ordinal()是枚举的默认顺序（从0开始），+1后与原ID保持一致
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }
}
