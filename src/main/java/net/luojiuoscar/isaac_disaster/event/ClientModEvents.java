package net.luojiuoscar.isaac_disaster.event;


import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.entity.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.renderer.chest.IsaacChestRenderer;
import net.luojiuoscar.isaac_disaster.block.renderer.chest.PedestalRenderer;
import net.luojiuoscar.isaac_disaster.client.FlyHudOverlay;
import net.luojiuoscar.isaac_disaster.client.ModKeyMappings;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.tnt.CustomTntRenderer;
import net.luojiuoscar.isaac_disaster.renderer.InvincibleChargeLayer;
import net.luojiuoscar.isaac_disaster.renderer.IsaacBulletRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    // 订阅实体渲染器注册事件
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(ModEntities.ISAAC_BOMB.get(), CustomTntRenderer::new);
        event.registerEntityRenderer(ModEntities.GIGA_BOMB.get(), CustomTntRenderer::new);
        event.registerEntityRenderer(ModEntities.TEAR_BULLET.get(), IsaacBulletRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NORMAL_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LOCKED_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);

    }

    // 注册自定义的模型
    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/chest_lid"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/locked_chest_lid"));
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

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.OPEN_ISAAC_ITEM_SCREEN);
    }











}
