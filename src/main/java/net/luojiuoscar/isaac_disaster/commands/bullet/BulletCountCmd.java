package net.luojiuoscar.isaac_disaster.commands.bullet;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.bullet.BulletRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Reports the number of transient optimized bullets currently simulated by the server. */
public final class BulletCountCmd {
    /** Registers {@code /isd bullet count} for operators and command sources with permission level 2. */
    public BulletCountCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("bullet")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("count")
                                .executes(context -> {
                                    int count = BulletRuntime.INSTANCE
                                            .activeCount(context.getSource().getServer());
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Active optimized bullets: " + count),
                                            false
                                    );
                                    // Keep the command successful even when the server currently has zero bullets.
                                    return 1;
                                }))));
    }
}
