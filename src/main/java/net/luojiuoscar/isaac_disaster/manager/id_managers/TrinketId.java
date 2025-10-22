package net.luojiuoscar.isaac_disaster.manager.id_managers;

public enum TrinketId {
    SWALLOWED_PENNY,
    AAA_BATTERY,
    BROKEN_REMOTE,
    CARTRIDGE,
    LUCKY_ROCK,
    LUCKY_TOE,
    CANCER,
    BLIND_RAGE,
    PERFECTION;


    private final int id;

    // 构造方法：自动生成递增的ID
    TrinketId() {
        this.id = ordinal();
    }

    // 获取ID的方法
    public int getId() {
        return id;
    }
}
