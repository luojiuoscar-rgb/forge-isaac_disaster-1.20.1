package net.luojiuoscar.isaac_disaster.commands.familiar;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
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
    private static final ResourceLocation SOURCE = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "command");
    private static final DynamicCommandExceptionType INVALID_FAMILIAR =
            new DynamicCommandExceptionType(id -> Component.literal("Invalid familiar type: " + id));

    private static final SuggestionProvider<CommandSourceStack> FAMILIAR_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(
                    FamiliarHelper.getRegisteredFamiliarTypeIds(),
                    builder
            );

    public FamiliarCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("familiar")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(countOperation("add", 1))
                                .then(countOperation("remove", -1))
                                .then(Commands.literal("clear")
                                        .executes(this::clearFamiliars)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> countOperation(String operation, int sign) {
        return Commands.literal(operation)
                .then(Commands.argument("type", ResourceLocationArgument.id())
                        .suggests(FAMILIAR_SUGGESTIONS)
                        .executes(context -> changeCount(context, sign))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                .executes(context -> changeCount(context,
                                        sign * IntegerArgumentType.getInteger(context, "count")))));
    }

    /**
     * Adds or removes requested familiar count, then immediately cleans invalid and excess entities.
     * Newly requested entities remain subject to the per-tick spawn limit.
     */
    private static int changeCount(CommandContext<CommandSourceStack> context, int delta)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        ResourceLocation type = ResourceLocationArgument.getId(context, "type");
        if (!FamiliarHelper.isRegisteredFamiliarType(type)) {
            throw INVALID_FAMILIAR.create(type);
        }

        long changedCount = 0;
        long recordedCount = 0;
        long effectiveCount = 0;
        for (ServerPlayer player : players) {
            PlayerFamiliarData data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                    .resolve().orElse(null);
            if (data == null) continue;
            int previousCount = data.getCount(type);
            if (delta >= 0) data.addCount(type, SOURCE, delta);
            else data.removeCount(type, SOURCE, -delta);
            changedCount += Math.abs((long) data.getCount(type) - previousCount);
            FamiliarHelper.maintainExistingFamiliars(player);
            recordedCount += data.getCount(type);
            effectiveCount += data.getEffectiveCount(type);
        }

        String action = delta >= 0 ? "Added" : "Removed";
        long amount = changedCount;
        long recorded = recordedCount;
        long effective = effectiveCount;
        context.getSource().sendSuccess(
                () -> Component.literal(action + " " + amount + " familiar(s) of " + type +
                        " across " + players.size() + " player(s). Recorded: " + recorded
                        + ", currently effective: " + effective + "."),
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
                FamiliarHelper.maintainExistingFamiliars(player);
            });
        }

        context.getSource().sendSuccess(
                () -> Component.literal("Cleared familiars for " + players.size() + " player(s)."),
                true
        );
        return players.size();
    }
}
