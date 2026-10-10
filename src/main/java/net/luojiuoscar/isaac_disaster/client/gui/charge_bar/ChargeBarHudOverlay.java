package net.luojiuoscar.isaac_disaster.client.gui.charge_bar;

import java.util.ArrayList;
import java.util.List;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.item.pickup.special.IsaacHead;
import net.luojiuoscar.isaac_disaster.registries.charge_bar.ChargeBarType;
import net.luojiuoscar.isaac_disaster.registries.charge_bar.ModChargeBars;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class ChargeBarHudOverlay {
    private static final int APPROACH_ALPHA = 128;

    public static final IGuiOverlay HUD_CHARGE_BAR =
            (forgeGui, graphics, partialTick, screenWidth, screenHeight) -> {
                var minecraft = Minecraft.getInstance();
                var player = minecraft.player;
                if (player == null || minecraft.options.hideGui) return;
                if (!(player.getMainHandItem().getItem() instanceof IsaacHead)
                        && !(player.getOffhandItem().getItem() instanceof IsaacHead)) return;
                IForgeRegistry<ChargeBarType> registry = RegistryManager.ACTIVE.getRegistry(
                        ModChargeBars.CHARGE_BAR_KEY.location());
                if (registry == null) return;

                var entries = new ArrayList<ChargeBarLayout.Entry>();
                ClientDataManager.getInstance().getChargeBars(partialTick).forEach((id, progress) -> {
                    if (!(progress > 0f)) return;
                    if (!registry.containsKey(id)) return;
                    ChargeBarType type = registry.getValue(id);
                    if (type != null) {
                        entries.add(new ChargeBarLayout.Entry(id, type, progress));
                    }
                });
                var sorted = ChargeBarLayout.sorted(entries);
                if (sorted.isEmpty()) return;
                long timeMillis = Util.getMillis();
                // Finish every white ring before any icon, including neighboring indicators.
                graphics.drawManaged(() -> renderIndicators(graphics, sorted,
                        screenWidth, screenHeight, timeMillis, true));
                // GuiGraphics.fill flushes unmanaged draws; batch all ring pixels together.
                graphics.drawManaged(() -> renderIndicators(graphics, sorted,
                        screenWidth, screenHeight, timeMillis, false));
            };

    private static void renderIndicators(GuiGraphics graphics, List<ChargeBarLayout.Entry> entries,
            int screenWidth, int screenHeight, long timeMillis, boolean approachOnly) {
        int size = ChargeRingGeometry.SIZE;
        for (int index = 0; index < entries.size(); index++) {
            var entry = entries.get(index);
            var position = ChargeBarLayout.position(index);
            if (position == null) continue;
            int x = (int) Math.round(screenWidth / 2.0 + position.x() - size / 2.0);
            int y = (int) Math.round(screenHeight / 2.0 + position.y() - size / 2.0);
            if (approachOnly) {
                renderApproachRing(graphics, entry.progress(), x + size / 2, y + size / 2);
            } else {
                renderRing(graphics, entry.type(), entry.progress(), x, y, timeMillis);
            }
        }
    }

    private static void renderApproachRing(GuiGraphics graphics, float progress, int centerX, int centerY) {
        int color = (APPROACH_ALPHA << 24) | 0xFFFFFF;
        for (var span : ChargeRingGeometry.rasterizeApproach(progress)) {
            graphics.fill(centerX + span.x(), centerY + span.y(),
                    centerX + span.x() + span.width(), centerY + span.y() + 1, color);
        }
    }

    private static void renderRing(GuiGraphics graphics, ChargeBarType type, float progress,
            int x, int y, long timeMillis) {
        int size = ChargeRingGeometry.SIZE;
        int fillColor = ChargeRingAnimation.fillColor(progress, type.fillColor(), timeMillis);
        graphics.blit(type.baseTexture(), x, y, 0f, 0f, size, size, size, size);
        for (var pixel : ChargeRingGeometry.rasterize(progress)) {
            if (pixel.part() != ChargeRingGeometry.Part.FILLED) continue;
            graphics.fill(x + pixel.x(), y + pixel.y(), x + pixel.x() + 1, y + pixel.y() + 1, fillColor);
        }
    }
}
