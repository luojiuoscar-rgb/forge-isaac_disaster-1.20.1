package net.luojiuoscar.isaac_disaster.block.custom.chest;

import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.ItemChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.OldChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OldChestBlock extends ItemChestBlock {

    public OldChestBlock(Properties properties) {
        super(properties, ModBlockEntities.OLD_CHEST_BLOCK_ENTITY::get);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OldChestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.OLD_CHEST_BLOCK_ENTITY.get(),
                ItemChestBlockEntity::tick);
    }

}
