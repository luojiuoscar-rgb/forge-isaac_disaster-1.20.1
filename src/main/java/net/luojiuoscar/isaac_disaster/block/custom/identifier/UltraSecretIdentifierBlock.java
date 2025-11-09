package net.luojiuoscar.isaac_disaster.block.custom.identifier;

import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.block_entity.identifier.IdentifierBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.identifier.UltraSecretIdentifierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class UltraSecretIdentifierBlock extends IdentifierBlock {

    public UltraSecretIdentifierBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UltraSecretIdentifierBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ULTRA_SECRET_IDENTIFIER_BLOCK_ENTITY.get(),
                IdentifierBlockEntity::tick);
    }
}
