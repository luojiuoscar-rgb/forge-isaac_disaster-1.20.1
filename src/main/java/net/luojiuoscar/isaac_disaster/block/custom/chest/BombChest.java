package net.luojiuoscar.isaac_disaster.block.custom.chest;

import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.BombChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.ItemChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BombChest extends ItemChestBlock {

    public BombChest(Properties properties) {
        super(properties, ModBlockEntities.BOMB_CHEST_BLOCK_ENTITY::get);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BombChestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BOMB_CHEST_BLOCK_ENTITY.get(),
                ItemChestBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide()|| !(level.getBlockEntity(pos) instanceof BombChestBlockEntity chest)) return InteractionResult.SUCCESS;

        if (!chest.isOpened() && chest.isLocked()) {
            player.displayClientMessage(
                    Component.translatable("block.isaac_disaster.bomb_chest.locked"), true);
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.CHEST_LOCKED,
                    SoundSource.BLOCKS, 1.0f ,1.0f);
        }else{
            return super.use(state, level, pos, player, hand, hit);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BombChestBlockEntity chest) {
           chest.setLocked(false); // 解锁
        }
    }
}
