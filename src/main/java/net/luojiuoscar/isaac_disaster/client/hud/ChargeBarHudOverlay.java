package net.luojiuoscar.isaac_disaster.client.hud;

import java.util.ArrayList;
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
    public static final IGuiOverlay HUD_CHARGE_BAR =
            (forgeGui, graphics, partialTick, screenWidth, screenHeight) -> {
                var minecraft = Minecraft.getInstance();
                var player = minecraft.player;
                if (player == null || minecraft.options.hideGui) return;
                IForgeRegistry<ChargeBarType> registry = RegistryManager.ACTIVE.getRegistry(
                        ModChargeBars.CHARGE_BAR_KEY.location());
                if (registry == null) return;

                boolean holdingIsaacHead = player.getMainHandItem().getItem() instanceof IsaacHead
                        || player.getOffhandItem().getItem() instanceof IsaacHead;
                var entries = new ArrayList<ChargeBarLayout.Entry>();
                ClientDataManager.getInstance().getChargeBars().forEach((id, progress) -> {
                    if (id.equals(ModChargeBars.ATTACK_CHARGE.getId()) && !holdingIsaacHead) return;
                    if (!registry.containsKey(id)) return;
                    ChargeBarType type = registry.getValue(id);
                    if (type != null) entries.add(new ChargeBarLayout.Entry(id, type, progress));
                });
                var sorted = ChargeBarLayout.sorted(entries);
                if (sorted.isEmpty()) return;
                long timeMillis = Util.getMillis();
                // GuiGraphics.fill flushes unmanaged draws; batch all ring pixels together.
                graphics.drawManaged(() -> {
                    for (int index = 0; index < sorted.size(); index++) {
                        var entry = sorted.get(index);
                        var type = entry.type();
                        var position = ChargeBarLayout.position(index);
                        if (position == null) continue;
                        int x = (int) Math.round(screenWidth / 2.0 + position.x() - ChargeRingGeometry.SIZE / 2.0);
                        int y = (int) Math.round(screenHeight / 2.0 + position.y() - ChargeRingGeometry.SIZE / 2.0);
                        renderRing(graphics, type, entry.progress(), x, y, timeMillis);
                    }
                });
            };

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
