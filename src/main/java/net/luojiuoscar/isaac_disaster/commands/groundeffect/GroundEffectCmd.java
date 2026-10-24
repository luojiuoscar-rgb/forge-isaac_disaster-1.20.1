package net.luojiuoscar.isaac_disaster.commands.groundeffect;

import com.mojang.brigadier.CommandDispatcher;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/** Debug commands for spawning and counting ordinary water puddles. */
public final class GroundEffectCmd {
    public GroundEffectCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("water")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("count")
                                .executes(context -> countWater(context.getSource())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> spawnWater(context.getSource(),
                                        EntityArgument.getPlayers(context, "targets"))))));
    }

    private static int countWater(CommandSourceStack source) {
        int count = GroundEffectRuntime.INSTANCE.activeCount(source.getServer());
        source.sendSuccess(() -> Component.literal("Active water puddles: " + count), false);
        return count;
    }

    private static int spawnWater(CommandSourceStack source, Collection<ServerPlayer> players) {
        int spawned = 0;
        for (ServerPlayer player : players) {
            if (GroundEffectRuntime.INSTANCE.spawnWater(player) != null) spawned++;
        }
        int count = spawned;
        source.sendSuccess(() -> Component.literal("Spawned " + count + " water puddle(s)."), true);
        return spawned;
    }
}
