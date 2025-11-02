package net.luojiuoscar.isaac_disaster.block.custom.chest;

import net.luojiuoscar.isaac_disaster.block.block_entity.chest.NormalChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class NormalChestBlock extends IsaacChestBlock {

    public NormalChestBlock(Properties properties) {
        super(properties, ModBlockEntities.NORMAL_CHEST_BLOCK_ENTITY::get);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NormalChestBlockEntity(pos, state);
    }
}
