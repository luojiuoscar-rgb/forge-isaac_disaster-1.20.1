package net.luojiuoscar.isaac_disaster.registries.bullet_visual;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Common material description for a billboard TearBullet visual. */
public final class TearBulletVisual extends BulletVisual {
    private final ResourceLocation texture;
    private final boolean acceptsTint;

    public TearBulletVisual(double priority, ResourceLocation texture, boolean acceptsTint) {
        super(BulletVisualTarget.TEAR, priority);
        this.texture = Objects.requireNonNull(texture, "texture");
        this.acceptsTint = acceptsTint;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public boolean acceptsTint() {
        return acceptsTint;
    }
}
