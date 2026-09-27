package net.luojiuoscar.isaac_disaster.commands.bullet;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.bullet.server.BulletRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Removes every optimized transient bullet from the loaded server dimensions. */
public final class BulletClearCmd {
    /** Registers {@code /isd bullet clear} for operator-level command sources. */
    public BulletClearCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("bullet")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("clear")
                                .executes(context -> {
                                    int removed = BulletRuntime.INSTANCE.clearAll(context.getSource().getServer());
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Cleared optimized bullets: " + removed), true);
                                    return 1;
                                }))));
    }
}
