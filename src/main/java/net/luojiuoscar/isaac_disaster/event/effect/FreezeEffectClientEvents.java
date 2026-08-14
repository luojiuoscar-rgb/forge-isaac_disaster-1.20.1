package net.luojiuoscar.isaac_disaster.event.effect;

import net.luojiuoscar.isaac_disaster.client.item_related.EntityRenderFreeze;
import net.luojiuoscar.isaac_disaster.system.freeze.state.TimeStopState;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.luojiuoscar.isaac_disaster.IsaacDisaster.MOD_ID;

/** Client Forge-bus handlers for freeze-only render and player-position state. */
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FreezeEffectClientEvents {
    private FreezeEffectClientEvents() {
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        EntityRenderFreeze.clear();
        TimeStopState.clearClientPlayerSnapshots();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            TimeStopState.clearClientPlayerSnapshots();
        } else {
            TimeStopState.tickClientPlayers(minecraft.level);
        }
    }
}
