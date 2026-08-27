package net.luojiuoscar.isaac_disaster.renderer.material;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Resolved client-side material inputs; geometry remains renderer-owned. */
public record BulletMaterial(
        ResourceLocation texture,
        RenderType renderType,
        float red,
        float green,
        float blue,
        float alpha
) {
    public static BulletMaterial tinted(ResourceLocation texture, RenderType renderType, int color, float alpha) {
        return new BulletMaterial(texture, renderType,
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                alpha);
    }
}
