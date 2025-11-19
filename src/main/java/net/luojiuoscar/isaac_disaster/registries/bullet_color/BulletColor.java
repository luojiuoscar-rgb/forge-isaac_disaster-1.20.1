package net.luojiuoscar.isaac_disaster.registries.bullet_color;

import org.joml.Vector3f;

public record BulletColor(int color, float alpha, int priority){

    public static Vector3f getVec3fColorById(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return new Vector3f(r, g, b);
    }
}
