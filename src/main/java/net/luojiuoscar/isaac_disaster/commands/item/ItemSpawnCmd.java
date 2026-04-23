package net.luojiuoscar.isaac_disaster.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootDataType;

public class ItemSpawnCmd {
    private static final SuggestionProvider<CommandSourceStack> LOOT_TABLE_SUGGESTIONS =
            (context, builder) -> {
                var server = context.getSource().getServer();

                return SharedSuggestionProvider.suggestResource(
                        server.getLootData().getKeys(LootDataType.TABLE).stream(),
                        builder
                );
            };


    public ItemSpawnCmd(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("isd")
                .then(Commands.literal("item")
                        .then(Commands.literal("spawn")

                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .suggests(LOOT_TABLE_SUGGESTIONS)

                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                                // 无 cost
                                                                .executes(ctx -> spawn(
                                                                        ctx,
                                                                        false,
                                                                        false,
                                                                        0
                                                                ))

                                                                // cost branch
                                                                .then(Commands.literal("cost")

                                                                        // default cost true + life
                                                                        .executes(ctx -> spawn(
                                                                                ctx,
                                                                                true,
                                                                                true,
                                                                                0
                                                                        ))

                                                                        .then(Commands.literal("life")
                                                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                                                        .executes(ctx -> spawn(
                                                                                                ctx,
                                                                                                true,
                                                                                                true,
                                                                                                IntegerArgumentType.getInteger(ctx, "value")
                                                                                        ))))

                                                                        .then(Commands.literal("money")
                                                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                                                        .executes(ctx -> spawn(
                                                                                                ctx,
                                                                                                true,
                                                                                                false,
                                                                                                IntegerArgumentType.getInteger(ctx, "value")
                                                                                        ))))
                                                                )
                                                        )))));
    }

    private int spawn(CommandContext<CommandSourceStack> ctx,
                      boolean cost,
                      boolean useLife,
                      int value) throws CommandSyntaxException {

        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");

        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");

        // 放置 pedestal 方块
        level.setBlock(pos,
                ModBlocks.PEDESTAL_BLOCK.get().defaultBlockState(),
                3);

        // 获取 BE
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PedestalBlockEntity pedestal)) {
            source.sendFailure(Component.literal("Failed to create pedestal block entity"));
            return 0;
        }

        // 基础设置 关闭decoration
        pedestal.setDecoration(false);

        // 设置 loot table
        pedestal.setItemLootTable(id.toString());

        // cost 逻辑
        if (cost) {
            if (useLife) {
                pedestal.setLifeCost(value);
                pedestal.setMoneyCost(0);
            } else {
                pedestal.setMoneyCost(value);
                pedestal.setLifeCost(0);
            }
        } else {
            pedestal.setLifeCost(0);
            pedestal.setMoneyCost(0);
        }

        // 同步更新
        pedestal.setChanged();
        level.sendBlockUpdated(pos,
                pedestal.getBlockState(),
                pedestal.getBlockState(),
                3);

        // feedback
        source.sendSuccess(
                () -> Component.literal(
                        "Spawned pedestal at " + pos +
                                " loot=" + id +
                                (cost ? (" cost=" + value + (useLife ? " life" : " money")) : "")
                ),
                true
        );


        return 1;
    }
}