package net.luojiuoscar.isaac_disaster.client.gui.attribute_indicator;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.system.attribute_indicator.AttributeSnapshot;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class AttributeIndicatorHudOverlay {
    private static final AttributeIndicatorState STATE = new AttributeIndicatorState();
    private static final ResourceLocation[] ICONS = {
            texture("movement_speed"), texture("fire_rate"), texture("damage"),
            texture("range"), texture("shot_speed"), texture("luck")
    };
    private static final ResourceLocation ARROW_UP = texture("arrow_up");
    private static final ResourceLocation ARROW_DOWN = texture("arrow_down");
    private static final int ARROW_SIZE = 6;
    private static final int TEXTURE_SIZE = 32;
    private static final int NORMAL_COLOR = 0xFFCCCCCC;
    private static final int INCREASE_COLOR = 0xFF70E060;
    private static final int DECREASE_COLOR = 0xFFE06060;

    private AttributeIndicatorHudOverlay() {
    }

    public static void update(AttributeSnapshot snapshot, boolean baseline) {
        STATE.accept(snapshot, baseline, Util.getMillis());
    }

    public static void clear() {
        STATE.clear();
    }

    public static final IGuiOverlay HUD_ATTRIBUTE_INDICATOR =
            (forgeGui, graphics, partialTick, screenWidth, screenHeight) -> {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player == null || minecraft.player.isSpectator()
                        || minecraft.options.hideGui || !STATE.hasSnapshot()) return;

                long nowMillis = Util.getMillis();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                try {
                    for (int row = 0; row < AttributeSnapshot.SIZE; row++) {
                        int y = AttributeIndicatorLayout.rowY(screenHeight, row);
                        graphics.blit(ICONS[row], AttributeIndicatorLayout.ICON_X, y + 2,
                                AttributeIndicatorLayout.ICON_SIZE, AttributeIndicatorLayout.ICON_SIZE,
                                0.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
                        int direction = STATE.direction(row, nowMillis);
                        int color = direction > 0 ? INCREASE_COLOR : direction < 0 ? DECREASE_COLOR : NORMAL_COLOR;
                        if (direction != 0) {
                            graphics.blit(direction > 0 ? ARROW_UP : ARROW_DOWN,
                                    AttributeIndicatorLayout.ARROW_X, y + (AttributeIndicatorLayout.ROW_HEIGHT - ARROW_SIZE) / 2,
                                    ARROW_SIZE, ARROW_SIZE, 0.0F, 0.0F,
                                    16, 16, 16, 16);
                        }
                        graphics.drawString(minecraft.font, STATE.formattedValue(row),
                                AttributeIndicatorLayout.VALUE_X,
                                y + (AttributeIndicatorLayout.ROW_HEIGHT - minecraft.font.lineHeight) / 2,
                                color, true);
                    }
                } finally {
                    RenderSystem.disableBlend();
                }
            };

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
                "textures/gui/attribute_indicator/" + name + ".png");
    }
}
