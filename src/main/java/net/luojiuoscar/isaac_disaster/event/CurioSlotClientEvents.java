package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.event.SlotModifiersUpdatedEvent;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.client.gui.CuriosScreenV2;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class CurioSlotClientEvents {
    private static boolean refreshRequested;

    private CurioSlotClientEvents() { }

    @SubscribeEvent
    public static void onSlotModifiersUpdated(SlotModifiersUpdatedEvent event) {
        if (event.getEntity() == Minecraft.getInstance().player) refreshRequested = true;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !refreshRequested) return;
        refreshRequested = false;
        Minecraft mc = Minecraft.getInstance();
        // Curios posts its update event before rebuilding menu slots; refresh widgets after the packet completes.
        if (mc.screen instanceof CuriosScreen || mc.screen instanceof CuriosScreenV2) {
            mc.screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        }
    }
}
