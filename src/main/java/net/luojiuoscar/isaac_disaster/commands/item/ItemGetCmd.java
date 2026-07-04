package net.luojiuoscar.isaac_disaster.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemGetCmd {

    private static final SuggestionProvider<CommandSourceStack> ITEM_SUGGESTIONS =
            (context, builder) -> SharedSuggestionProvider.suggestResource(
                    ForgeRegistries.ITEMS.getKeys().stream()
                            .filter(id -> ForgeRegistries.ITEMS.getValue(id) instanceof PassiveItem),
                    builder
            );

    public ItemGetCmd(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("item")
                        .then(Commands.literal("get")
                                .then(Commands.argument("item_id", ResourceLocationArgument.id())
                                        .suggests(ITEM_SUGGESTIONS)
                                        .executes(context -> {

                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            ResourceLocation id = ResourceLocationArgument.getId(context, "item_id");

                                            var item = ForgeRegistries.ITEMS.getValue(id);

                                            // 安全校验
                                            if (!(item instanceof PassiveItem pItem)) {
                                                context.getSource().sendFailure(
                                                        Component.literal("Invalid item type!")
                                                );
                                                return 0;
                                            }

                                            player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS).ifPresent(playerPassiveItem -> {
                                                int count = playerPassiveItem.getItemCountFromAll(player, pItem.getId());
                                                player.sendSystemMessage(
                                                        Component.literal("You have " + count + " of this item.")
                                                );
                                            });

                                            return 1;
                                        })))));
    }
}