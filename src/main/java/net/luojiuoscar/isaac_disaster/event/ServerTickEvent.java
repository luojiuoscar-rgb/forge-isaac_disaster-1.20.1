package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class ServerTickEvent {
    private static final int TICK_FREQUENCY = 4;

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

        // 每tickCounter执行一次
        if (tickCounter % ServerTickEvent.TICK_FREQUENCY == 0){
            // 获取服务器实例
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server == null) {
                server = event.player.getServer();
            }
            if (server == null) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()){
                // main
                chargeActiveItem(player);
                updateFly(player);
                bugsFix(player);
                recursiveItemTick(player);
            }
        }
    }

    private static void bugsFix(ServerPlayer player){
        // 防止无限无敌
        if (player.isInvulnerable() && player.getEffect(ModEffects.INVINCIBLE.get()) == null){
            player.setInvulnerable(false);
        }
    }

    private static void chargeActiveItem(ServerPlayer player){
        // 有4.5伏特时不执行充能
        if (PlayerHelper.hasItem(ItemId.VOLT_4P5.getId(), player)) return;


        // 遍历玩家所有物品槽位
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // 检查物品是否为NormalActiveItem类型且不为空
            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem) {
                ActiveItem.modifyCharge(stack, ServerTickEvent.TICK_FREQUENCY,
                        PlayerHelper.hasItem(ItemId.THE_BATTERY.getId(), player));
            }
        }
    }

    private static void updateFly(ServerPlayer player){
        if (player.isCreative() || player.isSpectator() || !PlayerHelper.canFly(player)) return;

        // 飞行故障修复（防止玩家可以飞但飞不起来）
        if (!player.getAbilities().mayfly){
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }

        // 正在飞
        if (player.getAbilities().flying && player.getEffect(ModEffects.TRANSCENDENCE.get()) == null){
            player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                    playerStatModifier -> playerStatModifier.addCurrentFlyTime(player, 4)
            );
        }else{
            player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                    playerStatModifier -> playerStatModifier.addCurrentFlyTime(player, -1)
            );
        }
    }

    private static void recursiveItemTick(ServerPlayer player){
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> playerPassiveItem.recursiveItemTick(player, TICK_FREQUENCY)
        );
    }
}
