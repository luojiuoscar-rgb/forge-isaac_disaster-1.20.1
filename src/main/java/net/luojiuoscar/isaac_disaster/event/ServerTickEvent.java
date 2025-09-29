package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.custom.NormalActiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;


@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ServerTickEvent {
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        // 只在服务器 Tick 结束阶段执行
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        if (server == null) {
            return;
        }

        updateCDs(server);

    }

    private void updateCDs(MinecraftServer server){
        // 创建迭代器进行遍历，避免并发修改异常
        Iterator<ItemStack> iterator = ItemListManager.ACTIVE_ITEMS_IN_CD_LIST.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            NormalActiveItem item = (NormalActiveItem) stack.getItem();

            int cd = item.getCurrentItemCD(stack);
            if (cd <= 0){
                boolean reachedMaxUseCount = item.modifyCurrentItemUseCount(stack, -1);
                if(reachedMaxUseCount){
                    // 使用迭代器删除，避免异常
                    iterator.remove();
                }else{
                    item.resetCD(stack);
                }
            }else{
                item.modifyCurrentItemCD(stack, -1);
            }

        }
    }
}
