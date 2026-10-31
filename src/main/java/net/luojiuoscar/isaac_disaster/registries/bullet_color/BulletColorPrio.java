package net.luojiuoscar.isaac_disaster.registries.bullet_color;

/** Built-in presets; extensions can supply their own tier and priority numbers. */
public enum BulletColorPrio {
    SPECTRAL(-1, 0),
    BASE(0, 0),
    REVELATION(0, 0),
    PARASITE(0, 10),
    DEAD_ONION(0, 20),
    URANUS(0, 90),
    BLOOD_TEAR(0, 100),
    SHOOP_DA_WHOOP(0, 100),
    IPECAC(0, 110),
    SPOON_BENDER(100, 0),
    SACRED_HEART(100, 1),
    POISON(200, 20),
    CHARM(200, 20),
    FEAR(200, 20),
    PETRIFIED(200, 20),
    DARK_MATTER(200, 20),
    FIRE_MIND(200, 20),
    HOLY_LIGHT(200, 20);

    private final int tier;
    private final double priority;

    BulletColorPrio(int tier, double priority) { this.tier = tier; this.priority = priority; }
    public int getTier() { return tier; }
    public double getPriority() { return priority; }
}
