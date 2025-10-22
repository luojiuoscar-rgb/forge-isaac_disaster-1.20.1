package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerSwallowedTrinketsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class getTrinketsCommand {
    public getTrinketsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("trinket")
                        .then(Commands.literal("get_all")
                                .executes(context -> {
                                    // 获取命令执行者（玩家）
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    player.getCapability(PlayerSwallowedTrinketsProvider.PLAYER_SWALLOWED_TRINKETS).ifPresent(
                                            playerSwallowedTrinkets -> {
                                                for (ItemStack stack : playerSwallowedTrinkets.getSwallowedTrinkets()){
                                                    player.sendSystemMessage(Component.literal(stack.toString()));
                                                }
                                            }
                                    );

                                    return 1;
                                }))));
    }
}
    