package net.luojiuoscar.isaac_disaster.client.hud;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item.pickup.special.IsaacHead;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ChargeBarHudOverlay {

    private static final ResourceLocation CHARGE_EMPTY =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "textures/hud/charge/charge_bar_empty.png");
    private static final ResourceLocation CHARGE_FULL =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "textures/hud/charge/charge_bar_full.png");

    private static final int BAR_WIDTH = 40;   // 横向长度
    private static final int BAR_HEIGHT = 40;   // 高度

    // 偏移准星下方
    private static final int OFFSET_X = -BAR_WIDTH / 2;
    private static final int OFFSET_Y = 0;

    public static final IGuiOverlay HUD_CHARGE_BAR =
            (forgeGui, guiGraphics, partialTick, screenWidth, screenHeight) -> {

                // 仅在手持IsaacHead的时候显示hud
                var player = net.minecraft.client.Minecraft.getInstance().player;
                if (player == null) return;
                if (!(player.getMainHandItem().getItem() instanceof IsaacHead)
                        && !(player.getOffhandItem().getItem() instanceof IsaacHead)) {
                    return;
                }

                float progress = ClientDataManager.getInstance().getChargeProgress();
                if (progress <= 0f) return;

                progress = Math.min(progress, 1.0f);

                int centerX = screenWidth / 2;
                int centerY = screenHeight / 2;

                int x = centerX + OFFSET_X;
                int y = centerY + OFFSET_Y;

                // 绘制底框（整条 empty）
                guiGraphics.blit(
                        CHARGE_EMPTY,
                        x, y,
                        0, 0,
                        BAR_WIDTH, BAR_HEIGHT,
                        BAR_WIDTH, BAR_HEIGHT
                );

                // 计算填充宽度
                int fillWidth = Math.round(BAR_WIDTH * progress);
                if (fillWidth > 0) {
                    // 只绘制满条的前 fillWidth 部分
                    guiGraphics.blit(
                            CHARGE_FULL,
                            x, y,
                            0, 0,
                            fillWidth, BAR_HEIGHT,
                            BAR_WIDTH, BAR_HEIGHT
                    );
                }
            };

}
