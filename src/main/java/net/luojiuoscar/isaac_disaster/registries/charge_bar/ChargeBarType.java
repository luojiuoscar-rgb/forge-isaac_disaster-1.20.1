package net.luojiuoscar.isaac_disaster.registries.charge_bar;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;

/** Server-safe ring style: a PNG base and a code-drawn angular fill. */
public final class ChargeBarType {
    private final int priority;
    private final ResourceLocation baseTexture;
    private final int fillColor;

    /** The square PNG covers the shared fixed-size ring template; fillColor is ARGB. */
    public ChargeBarType(int priority, ResourceLocation baseTexture, int fillColor) {
        this.priority = priority;
        this.baseTexture = baseTexture;
        this.fillColor = fillColor;
        if (baseTexture == null) {
            IsaacDisaster.LOGGER.warn("Skipping charge bar style with no base texture (priority={})", priority);
        }
    }

    public int priority() { return priority; }
    public boolean isValid() { return baseTexture != null; }
    public ResourceLocation baseTexture() { return baseTexture; }
    public int fillColor() { return fillColor; }
}
