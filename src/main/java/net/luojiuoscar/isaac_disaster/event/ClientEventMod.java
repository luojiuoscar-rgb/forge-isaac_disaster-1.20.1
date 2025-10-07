package net.luojiuoscar.isaac_disaster.event;


import net.luojiuoscar.isaac_disaster.client.FlyHudOverlay;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.tnt.CustomTntRenderer;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.SetRightClickC2SPacket;
import net.luojiuoscar.isaac_disaster.renderer.InvincibleChargeLayer;
import net.luojiuoscar.isaac_disaster.renderer.IsaacBulletRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventMod {
    // 订阅实体渲染器注册事件
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(ModEntities.ISAAC_BOMB.get(), CustomTntRenderer::new);
        event.registerEntityRenderer(ModEntities.GIGA_BOMB.get(), CustomTntRenderer::new);
        event.registerEntityRenderer(ModEntities.TEAR_BULLET.get(), IsaacBulletRenderer::new);

    }

    @SubscribeEvent
    public static void onLayerRegister(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new InvincibleChargeLayer<>(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "fly", FlyHudOverlay.HUD_FLY);
    }













}
