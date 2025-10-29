package net.luojiuoscar.isaac_disaster.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.worldgen.dynamic.DynamicDimensionManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public class DimensionCommands {

    public DimensionCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("isd")
                        .then(Commands.literal("dim")
                                .then(Commands.literal("create")
                                        .executes(ctx -> {
                                            boolean created = DynamicDimensionManager.createDynamicDimension(ctx.getSource().getServer()) != null;
                                            if (created) {
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.literal("动态维度已创建!"),
                                                        false
                                                );
                                            } else {
                                                ctx.getSource().sendFailure(
                                                        Component.literal("动态维度创建失败，可能已存在!")
                                                );
                                            }
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("delete")
                                        .executes(ctx -> {
                                            try {
                                                DynamicDimensionManager.unloadDynamicDimension(ctx.getSource().getServer());
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.literal("动态维度已卸载!"),
                                                        false
                                                );
                                            } catch (IOException e) {
                                                ctx.getSource().sendFailure(
                                                        Component.literal("卸载维度失败: " + e.getMessage())
                                                );
                                            }
                                            return 1;
                                        })
                                )
                        )
        );
    }
}
