package net.luojiuoscar.isaac_disaster.commands.player;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.helper.PoolHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class PlayerRefreshPoolCmd {
    public PlayerRefreshPoolCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("player")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.literal("refreshpool")
                                        .executes(context -> {

                                            Collection<ServerPlayer> players =
                                                    EntityArgument.getPlayers(context, "targets");

                                            for (ServerPlayer player : players) {
                                                PoolHelper.clear(player);

                                                player.sendSystemMessage(
                                                        Component.literal("已刷新你的道具池")
                                                );
                                            }

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("已刷新 " + players.size() + " 个玩家的道具池"),
                                                    true
                                            );

                                            return players.size();
                                        })))));
    }
}