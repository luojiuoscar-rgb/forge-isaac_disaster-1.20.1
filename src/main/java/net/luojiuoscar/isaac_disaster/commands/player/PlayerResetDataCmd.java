package net.luojiuoscar.isaac_disaster.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class PlayerResetDataCmd {
    public PlayerResetDataCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("player")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.literal("resetdata")
                                        .executes(context -> {

                                            Collection<ServerPlayer> players =
                                                    EntityArgument.getPlayers(context, "targets");

                                            for (ServerPlayer player : players) {
                                                PlayerHelper.resetAllAttributes(player);

                                                player.sendSystemMessage(
                                                        Component.literal("你的数据已被重置")
                                                );
                                            }

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("已重置 " + players.size() + " 个玩家的数据"),
                                                    true
                                            );

                                            return players.size();
                                        })))));
    }
}