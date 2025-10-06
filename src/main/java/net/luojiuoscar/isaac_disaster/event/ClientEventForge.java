package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
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

    /**
     * 让生物模型兼容自定义的大小缩放
     */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        float scale = ScaleUtils.getScale(event.getEntity());
        if (scale != 1.0F) {
            event.getPoseStack().scale(scale, scale, scale);
        }
    }
}
