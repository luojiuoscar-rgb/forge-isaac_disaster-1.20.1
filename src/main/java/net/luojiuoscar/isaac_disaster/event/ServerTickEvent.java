package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ServerTickEvent {
    private static final int CHARGE_FREQUENCY = 4;

    private static int tickCounter;

    @SubscribeEvent
    public static void onServerTick(TickEvent.PlayerTickEvent event) {
        // 只在服务器端处理，避免客户端和服务器重复执行
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) {
            return;
        }

        tickCounter++;
        // 重置计数器，防止整数溢出
        if (tickCounter >= Integer.MAX_VALUE - 10) {
            tickCounter = 0;
        }

        if (tickCounter % ServerTickEvent.CHARGE_FREQUENCY == 0){
            // 获取服务器实例
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server == null) {
                server = event.player.getServer();
            }
            if (server == null) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()){
                chargeActiveItem(player);
            }
        }


    }

    private static void chargeActiveItem(Player player){
        // 有4.5伏特时不执行充能
        AtomicInteger volt_4p5 = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> volt_4p5.set(playerPassiveItem.getItemCount(ItemId.VOLT_4P5.getId())));
        if (volt_4p5.get() > 0){
            return;
        }

        // 存储玩家的蓄电池数量
        AtomicInteger theBatteryCount = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> theBatteryCount.set(playerPassiveItem.getItemCount(ItemId.THE_BATTERY.getId())));

        // 遍历玩家所有物品槽位
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // 检查物品是否为NormalActiveItem类型且不为空
            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem) {
                ActiveItem.modifyCharge(stack, ServerTickEvent.CHARGE_FREQUENCY, theBatteryCount.get() > 0);
            }
        }
    }


}
