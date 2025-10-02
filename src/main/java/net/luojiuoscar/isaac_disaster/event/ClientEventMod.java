package net.luojiuoscar.isaac_disaster.event;


import net.luojiuoscar.isaac_disaster.entity.ModEntity;
import net.luojiuoscar.isaac_disaster.entity.tnt.CustomTntRenderer;
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
        // 注册自定义TNT渲染器：关联实体类型与渲染器
        event.registerEntityRenderer(
                ModEntity.ISAAC_BOMB.get(),  // 你的自定义TNT实体的EntityType实例
                CustomTntRenderer::new  // 渲染器的构造方法
        );
    }
}
