package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


public class SetTrinketEnchanted {
    public SetTrinketEnchanted(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd").then(Commands.literal("trinket").then(Commands.literal("setEnchanted")
                .executes(context -> {
                    // 获取命令执行者（玩家）
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    // 移除玩家的全部道具
                    ItemStack stack = player.getMainHandItem();
                    if (stack.isEmpty() || !(stack.getItem() instanceof Trinket)) return 0;
                    Trinket.setEnchanted(stack, true);

                    return 1;
                }))));
    }



}