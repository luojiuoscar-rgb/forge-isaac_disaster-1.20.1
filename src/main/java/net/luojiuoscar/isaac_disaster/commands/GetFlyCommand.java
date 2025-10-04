package net.luojiuoscar.isaac_disaster.commands;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
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
                    AtomicDouble flyTime = new AtomicDouble();
                    player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                            playerStatModifier -> flyTime.set(playerStatModifier.getFlyTime())
                    );
                    player.sendSystemMessage(Component.literal("FlyTime: " + flyTime));

                    AtomicDouble currentFlyTime = new AtomicDouble();
                    player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                            playerStatModifier -> currentFlyTime.set(playerStatModifier.getFlyTimeCurrent())
                    );
                    player.sendSystemMessage(Component.literal("CurrentFlyTime: " + currentFlyTime));

                    return 1;
                })));
    }



}