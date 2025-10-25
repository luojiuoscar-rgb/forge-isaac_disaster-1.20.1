package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicInteger;

public class GetItemCountCommand {
    public GetItemCountCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("passiveitems")
                        .then(Commands.literal("getCount")
                                // 添加一个整数参数，名称为"itemId"
                                .then(Commands.argument("itemId", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            // 获取命令执行者（玩家）
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            // 获取整数参数值
                                            int itemId = IntegerArgumentType.getInteger(context, "itemId");

                                            AtomicInteger count = new AtomicInteger();
                                            player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                                                    playerPassiveItem -> count.set(playerPassiveItem.getItemCountFromAll(player, itemId))
                                            );

                                            // 可以根据返回结果执行后续操作，例如输出调试信息
                                            player.sendSystemMessage(Component.literal("present? :"+player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).isPresent()));
                                            player.sendSystemMessage(Component.literal("UUID? :"+player.getUUID()));
                                            player.sendSystemMessage(Component.literal("name? :"+player.getName()));
                                            player.sendSystemMessage(Component.literal("itemId? :"+itemId));
                                            player.sendSystemMessage(Component.literal("count? :"+count.get()));

                                            return 1;
                                        })))));
    }
}
    