package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;


public class RefreshItemPoolCommand {
    public RefreshItemPoolCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd").then(Commands.literal("item_pool")
                .then(Commands.literal("refresh")
                .executes(context -> {
                    // 获取命令执行者（玩家）
                    ServerPlayer player = context.getSource().getPlayerOrException();

                    PoolHelper.clear(player);

                    player.sendSystemMessage(Component.literal("已刷新道具池"));

                    return 1;
                }))));
    }



}