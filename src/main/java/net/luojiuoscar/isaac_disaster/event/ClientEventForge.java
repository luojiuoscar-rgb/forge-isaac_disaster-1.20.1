package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventForge {
    /**
     * 玩家登出时清除客户端数据
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 只处理本地玩家的登出事件
        if (event.getEntity() == Minecraft.getInstance().player) {
            ClientDataManager.getInstance().reset();
        }
    }
}
