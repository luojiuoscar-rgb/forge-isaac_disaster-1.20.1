package net.luojiuoscar.isaac_disaster.entity.tnt;

public enum BombData {
    MEGA(7, 1.4f),
    NORMAL(4, 0.98f),
    SMALL(1, 0.4f);

    private final int power;
    private final float size;

    BombData(int power, float size){
        this.power = power;
        this.size = size;
    }

    public int power() {
        return power;
    }

    public float size() {
        return size;
    }
}
