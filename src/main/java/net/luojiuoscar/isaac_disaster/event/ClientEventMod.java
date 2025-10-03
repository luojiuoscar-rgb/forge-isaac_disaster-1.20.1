package net.luojiuoscar.isaac_disaster.event;


import net.luojiuoscar.isaac_disaster.entity.ModEntity;
import net.luojiuoscar.isaac_disaster.entity.tnt.CustomTntRenderer;
import net.luojiuoscar.isaac_disaster.entity.tnt.GigaBomb;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventMod {
    // 订阅实体渲染器注册事件
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册 IsaacBomb
        event.registerEntityRenderer(ModEntity.ISAAC_BOMB.get(), CustomTntRenderer::new);

        // 注册 GigaBomb
        event.registerEntityRenderer(ModEntity.GIGA_BOMB.get(), CustomTntRenderer::new);

    }
}
