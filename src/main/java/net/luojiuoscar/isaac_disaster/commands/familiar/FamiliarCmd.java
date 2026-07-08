package net.luojiuoscar.isaac_disaster.commands.familiar;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Debug commands for adding and removing Isaac familiars from player capability data.
 */
public class FamiliarCmd {
    private static final DynamicCommandExceptionType INVALID_FAMILIAR =
            new DynamicCommandExceptionType(id -> Component.literal("Invalid familiar type: " + id));

    private static final SuggestionProvider<CommandSourceStack> FAMILIAR_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(
                    FamiliarHelper.getRegisteredFamiliarTypeIds(context.getSource().getLevel()),
                    builder
            );

    public FamiliarCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("familiar")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("type", ResourceLocationArgument.id())
                                                .suggests(FAMILIAR_SUGGESTIONS)
                                                .executes(context -> changeCount(context, 1))
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                        .executes(context -> changeCount(
                                                                context,
                                                                IntegerArgumentType.getInteger(context, "count")
                                                        ))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("type", ResourceLocationArgument.id())
                                                .suggests(FAMILIAR_SUGGESTIONS)
                                                .executes(context -> changeCount(context, -1))
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                        .executes(context -> changeCount(
                                                                context,
                                                                -IntegerArgumentType.getInteger(context, "count")
                                                        ))))))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(this::clearFamiliars)))));
    }

    /**
     * Adds or removes requested familiar count, then immediately reconciles real familiar entities.
     */
    private static int changeCount(CommandContext<CommandSourceStack> context, int delta)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        ResourceLocation type = ResourceLocationArgument.getId(context, "type");
        if (!FamiliarHelper.canCreateFamiliar(context.getSource().getLevel(), type)) {
            throw INVALID_FAMILIAR.create(type);
        }

        for (ServerPlayer player : players) {
            player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(data -> {
                data.addCount(type, delta);
                FamiliarHelper.reconcile(player);
            });
        }

        String action = delta >= 0 ? "Added" : "Removed";
        int amount = Math.abs(delta);
        context.getSource().sendSuccess(
                () -> Component.literal(action + " " + amount + " familiar(s) of " + type +
                        " for " + players.size() + " player(s)."),
                true
        );
        return players.size();
    }

    /**
     * Clears all familiar requirements from the targets and discards existing familiar entities.
     */
    private int clearFamiliars(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");

        for (ServerPlayer player : players) {
            player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(data -> {
                data.clearRequirements();
                FamiliarHelper.reconcile(player);
            });
        }

        context.getSource().sendSuccess(
                () -> Component.literal("Cleared familiars for " + players.size() + " player(s)."),
                true
        );
        return players.size();
    }
}
