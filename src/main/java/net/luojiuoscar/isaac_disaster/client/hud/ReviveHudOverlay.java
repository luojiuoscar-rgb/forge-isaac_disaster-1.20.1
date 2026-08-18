package net.luojiuoscar.isaac_disaster.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

public final class ReviveHudOverlay {
    private static final int ICON_SIZE = 8;
    private static final int TEXTURE_SIZE = 16;

    private ReviveHudOverlay() {
    }

    public static final IGuiOverlay HUD_REVIVE = (forgeGui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }

        List<ResourceLocation> icons = ClientDataManager.getInstance().getReviveHudIcons();
        if (icons.isEmpty()) {
            return;
        }

        int leftX = screenWidth / 2 - 91;
        int y = screenHeight - forgeGui.leftHeight;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();

        for (int i = 0; i < icons.size(); i++) {
            int x = leftX + i * ICON_SIZE;
            guiGraphics.blit(icons.get(i), x, y, ICON_SIZE, ICON_SIZE,
                    0.0F, 0.0F, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        RenderSystem.disableBlend();
        forgeGui.leftHeight += ICON_SIZE;
    };
}
