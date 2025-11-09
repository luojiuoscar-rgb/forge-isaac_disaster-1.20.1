package net.luojiuoscar.isaac_disaster.block.custom.identifier;

import net.luojiuoscar.isaac_disaster.block.block_entity.identifier.IdentifierBlockEntity;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class IdentifierBlock extends BaseEntityBlock  {
    protected IdentifierBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && !player.isCreative()) return Shapes.empty();
        return Shapes.block();
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {}

    @Override
    public boolean canHarvestBlock(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Player player) {
        return player.isCreative(); // 仅创造模式可破坏
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {}

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof IdentifierBlockEntity identifier && !level.isClientSide()) {
                BlockData.get((ServerLevel) level).removeIdentifier(identifier.getResourceLocation(), pos);
            }
            super.onRemove(oldState, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type);
}
