package net.luojiuoscar.isaac_disaster.block.custom;

import net.luojiuoscar.isaac_disaster.block.block_entity.IsaacChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IsaacChestBlock extends ChestBlock {

    public IsaacChestBlock(Properties properties) {
        super(properties, ModBlockEntities.CHEST_BLOCK_ENTITY::get);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IsaacChestBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // 覆盖双箱逻辑
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IsaacChestBlockEntity chest) return chest;
        return null;
    }
}
