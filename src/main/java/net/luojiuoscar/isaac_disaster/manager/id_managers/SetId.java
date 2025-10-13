package net.luojiuoscar.isaac_disaster.manager.id_managers;

public enum SetId {
    SPUN,
    ADULT;


    private final int id;

    // 构造方法：自动生成递增的ID
    SetId() {
        this.id = ordinal();
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }
}
