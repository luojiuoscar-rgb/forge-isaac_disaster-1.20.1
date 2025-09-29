package net.luojiuoscar.isaac_disaster.manager;

public enum ItemId {
    BREAKFAST,
    DESSERT,
    WOODEN_SPOON,
    STEVEN,
    CRICKETS_HEAD,
    THE_COMMON_COLD,
    GLASS_EYE,
    YUM_HEART;

    private final int id;

    // 构造方法：自动生成递增的ID
    ItemId() {
        this.id = ordinal() + 1; // ordinal()是枚举的默认顺序（从0开始），+1后与原ID保持一致
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }
}
