package net.luojiuoscar.isaac_disaster.bullet.core;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Immutable visual and geometric properties shared by one bullet for its lifetime. */
public final class BulletProfile {
    private final double renderScale;
    private final double collisionWidth;
    private final double collisionHeight;
    private final int color;
    private final float alpha;
    private final ResourceLocation colorId;
    private final Set<ResourceLocation> visualIds;

    public BulletProfile(
        double renderScale,
        double collisionWidth,
        double collisionHeight,
        int color,
        float alpha,
        @Nullable ResourceLocation colorId,
        Set<ResourceLocation> visualIds) {
        this.renderScale = Math.max(0.0D, renderScale);
        this.collisionWidth = Math.max(0.0D, collisionWidth);
        this.collisionHeight = Math.max(0.0D, collisionHeight);
        this.color = color;
        this.alpha = Math.max(0.0F, Math.min(1.0F, alpha));
        this.colorId = colorId;
        this.visualIds = visualIds == null ? Set.of() : Set.copyOf(visualIds);
    }

    public double renderScale() {
        return renderScale;
    }

    public double collisionWidth() {
        return collisionWidth;
    }

    public double collisionHeight() {
        return collisionHeight;
    }

    public int color() {
        return color;
    }

    public float alpha() {
        return alpha;
    }

    @Nullable
    public ResourceLocation colorId() {
        return colorId;
    }

    public Set<ResourceLocation> visualIds() {
        return visualIds;
    }
}
