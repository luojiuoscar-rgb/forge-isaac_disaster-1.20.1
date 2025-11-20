package net.luojiuoscar.isaac_disaster.manager.attack;

public enum ModAttackType {
    BULLET(0),
    LASER(100),
    IPECAC(10);


    private final int id;
    private final double priority;

    // ================= 构造与字段 =================
    ModAttackType(int priority) {
        this.id = this.ordinal();
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public double getPriority() {
        return priority;
    }
}
