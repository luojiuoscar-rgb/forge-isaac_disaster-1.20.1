package net.luojiuoscar.isaac_disaster.registries.bullet_visual;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Common material description for a FetusBullet visual. */
public final class FetusBulletVisual extends BulletVisual {
    @Nullable
    private final ResourceLocation texture;
    private final boolean acceptsTint;

    public FetusBulletVisual(double priority, @Nullable ResourceLocation texture, boolean acceptsTint) {
        super(BulletVisualTarget.FETUS, priority);
        this.texture = texture;
        this.acceptsTint = acceptsTint;
    }

    @Nullable
    public ResourceLocation getTexture() {
        return texture;
    }

    public boolean acceptsTint() {
        return acceptsTint;
    }
}
