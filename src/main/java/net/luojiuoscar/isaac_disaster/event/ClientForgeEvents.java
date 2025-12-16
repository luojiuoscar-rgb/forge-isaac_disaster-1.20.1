package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.client.ModKeyMappings;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.OpenIsaacItemScreenC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.SetRightClickC2SPacket;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    /**
     * 玩家登出时清除客户端数据
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 只处理本地玩家的登出事件
        if (event.getEntity() == Minecraft.getInstance().player) {
            ClientDataManager.getInstance().init();
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

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (event.getButton() == 1) { // 1 = 右键
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // 如果此时正在打开任何GUI（包括背包、菜单、聊天框等），则取消右键逻辑
            if (mc.screen != null){
                ModMessages.sendToServer(new SetRightClickC2SPacket(false));
                return;
            }


            if (event.getAction() == 1) { // 按下
                ModMessages.sendToServer(new SetRightClickC2SPacket(true));
            } else if (event.getAction() == 0) { // 松开
                ModMessages.sendToServer(new SetRightClickC2SPacket(false));
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (ModKeyMappings.OPEN_ISAAC_ITEM_SCREEN.isDown()) {
            ModMessages.sendToServer(new OpenIsaacItemScreenC2SPacket());
        }
    }




}
