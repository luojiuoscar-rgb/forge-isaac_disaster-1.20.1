package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class HasItemCommand {
    public HasItemCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("passiveitems")
                        .then(Commands.literal("has")
                                // 添加一个整数参数，名称为"itemId"
                                .then(Commands.argument("itemId", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            // 获取命令执行者（玩家）
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            // 获取整数参数值
                                            int itemId = IntegerArgumentType.getInteger(context, "itemId");

                                            // 调用PlayerHelper.hasItem方法
                                            boolean hasItem = PlayerHelper.hasItem(itemId, player);

                                            // 可以根据返回结果执行后续操作，例如输出调试信息
                                            if (hasItem) {
                                                player.sendSystemMessage(Component.literal("拥有ID为 " + itemId + " 的物品"));
                                            } else {
                                                player.sendSystemMessage(Component.literal("不拥有ID为 " + itemId + " 的物品"));
                                            }

                                            return 1;
                                        })))));
    }
}
    