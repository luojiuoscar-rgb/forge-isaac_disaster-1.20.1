package net.luojiuoscar.isaac_disaster.manager.attack.managers;

import org.joml.Vector3f;

public enum BulletColor {
    BASE(0xFFFFFF, 1.0f, 0),
    SPOON_BENDER(0x7A33C0, 1.0f, 1),
    BLOOD_TEAR(0xCC171F, 1.0f, 5),
    POISON(0x5CA45C, 1.0f, 20);




    private final int id;
    private final int color;
    private final float alpha;
    private final int priority;

    BulletColor(int color, float alpha, int priority) {
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

    public double getPriority() {
        return priority;
    }

    public static BulletColor byId(int id) {
        for (BulletColor value : values()) {
            if (value.id == id) return value;
        }
        return BASE;
    }

    public static double getPriorityById(int id) {
        BulletColor value = byId(id);
        return value.getPriority();
    }

    public static int getColorById(int id) {
        BulletColor value = byId(id);
        return value.getColor();
    }

    public static float getAlphaById(int id) {
        BulletColor value = byId(id);
        return value.getAlpha();
    }

    public static Vector3f getVec3fColorById(int id) {
        int color = getColorById(id);
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new Vector3f(r, g, b);
    }

}
