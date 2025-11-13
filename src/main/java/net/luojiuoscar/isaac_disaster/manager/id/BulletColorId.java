package net.luojiuoscar.isaac_disaster.manager.id;

public enum BulletColorId {
    BASE(0xFFFFFF, 1.0f, 0),
    SPOON_BENDER(0x7A33C0, 1.0f, 1),
    BLOOD_TEAR(0xCC171F, 1.0f, 5),
    POISON(0x5CA45C, 1.0f, 20);




    private final int id;
    private final int color;
    private final float alpha;
    private final int priority;

    BulletColorId(int color, float alpha, int priority) {
        this.id = ordinal();
        this.color = color;
        this.alpha = alpha;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public float getAlpha() {
        return alpha;
    }

    public int getPriority() {
        return priority;
    }

    public static BulletColorId byId(int id) {
        for (BulletColorId value : values()) {
            if (value.id == id) return value;
        }
        return BASE;
    }

    public static int getPriorityById(int id) {
        BulletColorId value = byId(id);
        return value.getPriority();
    }

    public static int getColorById(int id) {
        BulletColorId value = byId(id);
        return value.getColor();
    }

    public static float getAlphaById(int id) {
        BulletColorId value = byId(id);
        return value.getAlpha();
    }

}
