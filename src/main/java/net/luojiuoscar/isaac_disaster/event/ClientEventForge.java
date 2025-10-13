package net.luojiuoscar.isaac_disaster.event;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.SetRightClickC2SPacket;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.PlayerEvent;

import static com.mojang.text2speech.Narrator.LOGGER;

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
}
