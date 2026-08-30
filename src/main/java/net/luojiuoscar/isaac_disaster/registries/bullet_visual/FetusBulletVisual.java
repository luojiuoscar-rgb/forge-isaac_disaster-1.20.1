package net.luojiuoscar.isaac_disaster.registries.bullet_visual;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Common material description for a FetusBullet visual. */
public final class FetusBulletVisual extends BulletVisual {
    private static final ResourceLocation DEFAULT_SHATTER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/rotten_flesh.png");
    @Nullable
    private final ResourceLocation texture;
    private final boolean acceptsTint;
    @Nullable
    private final ResourceLocation shatterTexture;

    public FetusBulletVisual(double priority, @Nullable ResourceLocation texture, boolean acceptsTint,
                             @Nullable ResourceLocation shatterTexture) {
        super(BulletVisualTarget.FETUS, priority);
        this.texture = texture;
        this.acceptsTint = acceptsTint;
        this.shatterTexture = shatterTexture;
    }

    @Nullable
    public ResourceLocation getTexture() {
        return texture;
    }

    public boolean acceptsTint() {
        return acceptsTint;
    }

    public ResourceLocation getShatterTexture() {
        return shatterTexture == null ? DEFAULT_SHATTER_TEXTURE : shatterTexture;
    }
}
