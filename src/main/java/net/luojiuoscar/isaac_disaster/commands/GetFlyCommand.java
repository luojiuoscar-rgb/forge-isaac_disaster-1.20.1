package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;


public class GetFlyCommand {
    public GetFlyCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd").then(Commands.literal("getFly")
                .executes(context -> {
                    // 获取命令执行者（玩家）
                    ServerPlayer player = context.getSource().getPlayerOrException();

                    // 移除玩家的全部道具
                    double flyTime = PlayerHelper.getFly(player);
                    player.sendSystemMessage(Component.literal("FlyTime: " + flyTime));

                    double[] currentFlyTime = {0};
                    player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                            playerStatModifier -> currentFlyTime[0] = playerStatModifier.getFlyTimeCurrent()
                    );
                    player.sendSystemMessage(Component.literal("CurrentFlyTime: " + currentFlyTime[0]));

                    return 1;
                })));
    }



}