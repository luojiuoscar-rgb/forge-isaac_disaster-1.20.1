package net.luojiuoscar.isaac_disaster.block.custom.identifier;

import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.block_entity.identifier.AngelIdentifierBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.identifier.IdentifierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AngelIdentifierBlock extends IdentifierBlock {

    public AngelIdentifierBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AngelIdentifierBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ANGEL_IDENTIFIER_BLOCK_ENTITY.get(),
                IdentifierBlockEntity::tick);
    }
}
