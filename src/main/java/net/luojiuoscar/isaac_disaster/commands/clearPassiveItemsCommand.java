package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.manager.ItemManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.ClearPassiveItemC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.RemovePassiveItemFromIdC2SPacket;
import net.luojiuoscar.isaac_disaster.passive_item.PassiveItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;


public class clearPassiveItemsCommand {
    public clearPassiveItemsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd").then(Commands.literal("passiveitems").then(Commands.literal("clear")
                .executes(context -> {
                    // 获取命令执行者（玩家）
                    Player player = context.getSource().getPlayerOrException();
                    // 移除玩家的全部道具
                    player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                            playerPassiveItem -> {playerPassiveItem.clearPlayerPassiveItems(player);
                            });

                    return 1;
                }))));
    }



}