package net.luojiuoscar.isaac_disaster.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class FlyHudOverlay {
    private static final ResourceLocation FLY_FULL = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
            "textures/hud/fly/fly_full.png");
    private static final ResourceLocation FLY_HALF = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
            "textures/hud/fly/fly_half.png");
    private static final ResourceLocation FLY_EMPTY = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID,
            "textures/hud/fly/fly_empty.png");

    private static final int ICON_SIZE = 8;

    public static final IGuiOverlay HUD_FLY = (forgeGui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        LocalPlayer pl = Minecraft.getInstance().player;

        // 不显示hud的情况
        if (pl == null || pl.isCreative() || pl.isSpectator() || !PlayerHelper.canFly(pl)) return;


        int flyUnits = ClientDataManager.getInstance().getFlyUnits(); // 0 - 20

        int maxUnits = 20;
        int units = Math.max(Math.min(flyUnits, maxUnits), 0);

        int fullCount = units / 2;           // 右侧完整格数
        boolean hasHalf = (units % 2) == 1;  // 是否有半格

        // 右侧基准
        int rightX = screenWidth / 2 + 3;
        // 垂直位置使用 Forge 提供的堆叠高度，紧贴在饥饿条上方
        int y = screenHeight - forgeGui.rightHeight;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();

        // 从右往左绘制；i=0 是最右侧
        for (int i = 0; i < 10; i++) {
            int iconX = rightX + ICON_SIZE * (10 - i);

            ResourceLocation tex;
            if (i < fullCount) {
                tex = FLY_FULL;
            } else if (i == fullCount && hasHalf) {
                tex = FLY_HALF;
            } else {
                tex = FLY_EMPTY;
            }

            guiGraphics.blit(tex, iconX, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        RenderSystem.disableBlend();

        // 避免与其它 overlay 重叠
        forgeGui.rightHeight += ICON_SIZE;
    };
}
