package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class TriggerPillEffectCount {
    public TriggerPillEffectCount(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("pill")
                        .then(Commands.literal("trigger")
                                // 添加整数参数effectId
                                .then(Commands.argument("effectId", IntegerArgumentType.integer())
                                        // 添加可选的boolean参数flag
                                        .then(Commands.argument("flag", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                                    int effectId = IntegerArgumentType.getInteger(context, "effectId");
                                                    boolean flag = BoolArgumentType.getBool(context, "flag");

                                                    if (flag){
                                                        PillEffectManager.getInstance()
                                                                .getEffectFromEffectId(effectId)
                                                                .onUseH(player, true);
                                                    }else{
                                                        PillEffectManager.getInstance()
                                                                .getEffectFromEffectId(effectId)
                                                                .onUse(player, true);
                                                    }
                                                    return 1;
                                                }))))));
    }
}
