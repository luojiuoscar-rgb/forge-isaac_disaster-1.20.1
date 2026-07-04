package net.luojiuoscar.isaac_disaster.commands.trinket;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class TrinketClearSwallowedCmd {

    public TrinketClearSwallowedCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("trinket")
                        .then(Commands.literal("clear")
                                .then(Commands.literal("swallowed")
                                        .executes(context -> {

                                            ServerPlayer player = context.getSource().getPlayerOrException();

                                            player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS)
                                                    .ifPresent(playerPassiveItem -> playerPassiveItem.clear(player));

                                            player.sendSystemMessage(
                                                    Component.literal("被吞的 Trinket 已全部清除")
                                            );

                                            return 1;
                                        })))));
    }
}
