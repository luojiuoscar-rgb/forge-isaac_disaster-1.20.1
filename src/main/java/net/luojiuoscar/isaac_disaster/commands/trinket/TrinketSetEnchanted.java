package net.luojiuoscar.isaac_disaster.commands.trinket;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TrinketSetEnchanted {

    public TrinketSetEnchanted(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("trinket")
                        .then(Commands.literal("enchantment")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(context -> {

                                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                                    ItemStack stack = player.getMainHandItem();

                                                    boolean value = BoolArgumentType.getBool(context, "value");

                                                    if (stack.isEmpty()) {
                                                        context.getSource().sendFailure(
                                                                Component.literal("请手持一个 Trinket")
                                                        );
                                                        return 0;
                                                    }

                                                    if (!(stack.getItem() instanceof Trinket)) {
                                                        context.getSource().sendFailure(
                                                                Component.literal("该物品不是 Trinket")
                                                        );
                                                        return 0;
                                                    }

                                                    Trinket.setEnchanted(stack, value);

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Trinket 附魔状态已设置为 " + value),
                                                            false
                                                    );

                                                    return 1;
                                                }))))));
    }
}