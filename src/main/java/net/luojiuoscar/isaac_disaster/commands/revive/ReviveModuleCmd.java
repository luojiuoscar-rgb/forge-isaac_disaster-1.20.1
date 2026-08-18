package net.luojiuoscar.isaac_disaster.commands.revive;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ModReviveModule;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ReviveModule;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.Collection;

public class ReviveModuleCmd {
    private static final DynamicCommandExceptionType INVALID_MODULE =
            new DynamicCommandExceptionType(id -> Component.literal("Invalid revive module: " + id));

    private static final SuggestionProvider<CommandSourceStack> MODULE_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(
                    getRegistry().getKeys().stream(),
                    builder
            );

    public ReviveModuleCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("revive_module")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.literal("add")
                                        .then(Commands.argument("module", ResourceLocationArgument.id())
                                                .suggests(MODULE_SUGGESTIONS)
                                                .executes(context -> change(context, 1))
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                        .executes(context -> change(
                                                                context,
                                                                scaleDelta(1, IntegerArgumentType.getInteger(context, "count"))
                                                        )))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("module", ResourceLocationArgument.id())
                                                .suggests(MODULE_SUGGESTIONS)
                                                .executes(context -> change(context, -1))
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                        .executes(context -> change(
                                                                context,
                                                                scaleDelta(-1, IntegerArgumentType.getInteger(context, "count"))
                                                        )))))
                                .then(Commands.literal("clear")
                                        .executes(this::clearAll)
                                        .then(Commands.argument("module", ResourceLocationArgument.id())
                                                .suggests(MODULE_SUGGESTIONS)
                                                .executes(this::clearModule))))));
    }

    private static int change(CommandContext<CommandSourceStack> context, int delta)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        ResourceLocation id = ResourceLocationArgument.getId(context, "module");
        requireRegistered(id);

        for (ServerPlayer player : players) {
            StatManager.addReviveModuleProvider(player, id, delta);
            StatManager.addReviveModuleConsumer(player, id, delta);
        }

        String action = delta > 0 ? "Added" : "Removed";
        int amount = Math.abs(delta);
        context.getSource().sendSuccess(
                () -> Component.literal(action + " " + amount + " revive module(s) of " + id +
                        " for " + players.size() + " player(s)."),
                true
        );
        return players.size();
    }

    private static int scaleDelta(int deltaPerEntry, int count) {
        return deltaPerEntry * count;
    }

    private int clearAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        for (ServerPlayer player : players) {
            player.getCapability(EffectModulesProvider.EFFECT_MODULES)
                    .ifPresent(effectModules -> {
                        effectModules.getReviveSequence().clear();
                        ForgeEvents.syncReviveHudToClient(player);
                    });
        }

        context.getSource().sendSuccess(
                () -> Component.literal("Cleared all revive modules for " + players.size() + " player(s)."),
                true
        );
        return players.size();
    }

    private int clearModule(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        ResourceLocation id = ResourceLocationArgument.getId(context, "module");
        requireRegistered(id);

        for (ServerPlayer player : players) {
            player.getCapability(EffectModulesProvider.EFFECT_MODULES)
                    .ifPresent(effectModules -> {
                        effectModules.getReviveSequence().clear(id);
                        ForgeEvents.syncReviveHudToClient(player);
                    });
        }

        context.getSource().sendSuccess(
                () -> Component.literal("Cleared revive module " + id + " for " + players.size() + " player(s)."),
                true
        );
        return players.size();
    }

    private static void requireRegistered(ResourceLocation id) throws CommandSyntaxException {
        if (getRegistry().getValue(id) == null) {
            throw INVALID_MODULE.create(id);
        }
    }

    private static IForgeRegistry<ReviveModule> getRegistry() {
        return RegistryManager.ACTIVE.getRegistry(ModReviveModule.REVIVE_MODULE_KEY);
    }
}
