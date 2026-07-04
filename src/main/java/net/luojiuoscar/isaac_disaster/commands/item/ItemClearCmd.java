package net.luojiuoscar.isaac_disaster.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class ItemClearCmd {
    public ItemClearCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("item")
                        .then(Commands.literal("clear")
                                .executes(context -> {

                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(
                                            playerPassiveItem -> {
                                                playerPassiveItem.clearPlayerPassiveItems(player);
                                            });

                                    return 1;
                                }))));
    }
}