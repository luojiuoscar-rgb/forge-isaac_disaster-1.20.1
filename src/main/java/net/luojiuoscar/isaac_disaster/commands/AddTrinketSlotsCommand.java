package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.luojiuoscar.isaac_disaster.helper.CuriosHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;


public class AddTrinketSlotsCommand {
    public AddTrinketSlotsCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd").then(Commands.literal("trinket").then(Commands.literal("add_slots")
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                .executes(context -> {
                    // 获取命令执行者（玩家）
                    ServerPlayer player = context.getSource().getPlayerOrException();

                    int amount = IntegerArgumentType.getInteger(context, "amount");
                    CuriosHelper.addPermanentSlotModifier(player, CuriosHelper.TRINKET, CuriosHelper.ISAAC_TRINKET_SLOT_MODIFIER_UUID,
                            "isaac_trinket", amount);
                    return 1;
                })))));
    }



}