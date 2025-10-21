package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;


public class ClearSwallowedTrinkets {
    public ClearSwallowedTrinkets(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd").then(Commands.literal("trinket").then(Commands.literal("clear")
                .executes(context -> {
                    // 获取命令执行者（玩家）
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    // 移除玩家的全部道具
                    player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                            playerSwallowedTrinkets -> {playerSwallowedTrinkets.clearAll(player);
                            });

                    player.sendSystemMessage(Component.literal("被吞的被动道具已经全部清除"));

                    return 1;
                }))));
    }



}