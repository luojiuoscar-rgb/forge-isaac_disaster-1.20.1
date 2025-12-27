package net.luojiuoscar.isaac_disaster.registries.attack_type;

public enum AttackPrio {
    BULLET(0),
    NEPTUNUS(10),
    LASER(100),
    CURSED_EYE(150),
    C_SECTION(200),
    BRIMSTONE(300);

    private final double priority;

    AttackPrio(double priority){
        this.priority = priority;
    }

    public double getPriority() {
        return priority;
    }
}
