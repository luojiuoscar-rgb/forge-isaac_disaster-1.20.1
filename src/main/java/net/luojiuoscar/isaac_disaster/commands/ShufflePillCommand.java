package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


public class ShufflePillCommand {
    public ShufflePillCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd").then(Commands.literal("pill").then(Commands.literal("shuffle")
                .executes(context -> {
                    // 获取命令执行者（玩家）
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ServerLevel level = (ServerLevel) player.level();

                    PillEffectManager.getInstance().shufflePills(level);
                    player.sendSystemMessage(Component.literal("药丸重置！"));

                    return 1;
                }))));
    }



}